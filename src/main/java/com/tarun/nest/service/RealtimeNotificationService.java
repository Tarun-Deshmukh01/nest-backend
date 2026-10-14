package com.tarun.nest.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarun.nest.dto.NotificationResponse;
import com.tarun.nest.dto.RealtimeNotification;
import com.tarun.nest.entity.User;
import com.tarun.nest.entity.UserNotification;
import com.tarun.nest.entity.Role;
import com.tarun.nest.repository.UserNotificationRepository;
import com.tarun.nest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class RealtimeNotificationService {

    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final UserNotificationRepository notificationRepository;
    private final Map<Long, Set<WebSocketSession>> sessionsByUserId = new ConcurrentHashMap<>();

    private static final int MAX_PAGE_SIZE = 100;

    public void registerSession(Long userId, WebSocketSession session) {
        sessionsByUserId.computeIfAbsent(userId, ignored -> ConcurrentHashMap.newKeySet()).add(session);
    }

    public void unregisterSession(WebSocketSession session) {
        sessionsByUserId.values().forEach(sessions -> sessions.remove(session));
    }

    public void sendNotification(WebSocketSession session, RealtimeNotification notification) {
        send(session, serialize(notification));
    }

    @Transactional
    public void sendNotificationToUsers(Collection<Long> userIds, String title, String message, String href) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }

        String eventKey = UUID.randomUUID().toString();
        userRepository.findAllById(userIds.stream().filter(Objects::nonNull).distinct().toList())
                .forEach(user -> persistNotification(
                        user, "GENERAL", title, message, null, href, eventKey
                ));
    }

    @Transactional
    public void sendNotificationToAdmins(String title, String message, String href) {
        sendNotificationToAdmins(UUID.randomUUID().toString(), title, message, href);
    }

    @Transactional
    public void sendNotificationToAdmins(String notificationId, String title, String message, String href) {
        var adminUsers = userRepository.findByRoleAndActiveTrue(Role.ADMIN);

        if (adminUsers.isEmpty()) {
            log.warn("No admin users found for event key {}", notificationId);
            return;
        }

        adminUsers.stream()
                .filter(user -> user.getId() != null)
                .forEach(admin -> persistNotification(
                        admin, "GENERAL", title, message, null, href, notificationId
                ));
    }

    @Transactional
    public void notifyAdminsOfVendorApplication(Long vendorId, String vendorName) {
        String eventKey = "vendor-application:" + vendorId;
        String name = vendorName == null || vendorName.isBlank() ? "A vendor" : vendorName;
        String title = "New vendor request received";
        String message = name + " submitted a registration request.";

        userRepository.findByRoleAndActiveTrue(Role.ADMIN).stream()
                .filter(admin -> admin.getId() != null)
                .forEach(admin -> persistNotification(
                        admin, "VENDOR_APPLICATION", title, message, vendorId,
                        "/admin/vendor-requests", eventKey
                ));
    }

    @Transactional
    public void sendNotificationToRole(Role role, String title, String message, String href) {
        List<Long> userIds = userRepository.findByRole(role).stream()
                .map(User::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        sendNotificationToUsers(userIds, title, message, href);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotifications(Long userId, Pageable pageable) {
        Pageable boundedPage = PageRequest.of(
                Math.max(pageable.getPageNumber(), 0),
                Math.min(Math.max(pageable.getPageSize(), 1), MAX_PAGE_SIZE)
        );
        return notificationRepository.findByRecipient_IdOrderByCreatedAtDescIdDesc(userId, boundedPage)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByRecipient_IdAndReadStatusFalse(userId);
    }

    @Transactional
    public java.util.Optional<NotificationResponse> markAsRead(Long notificationId, Long userId) {
        return notificationRepository.findByIdAndRecipient_Id(notificationId, userId)
                .map(notification -> {
                    if (!notification.isReadStatus()) {
                        notification.setReadStatus(true);
                        notification.setReadAt(LocalDateTime.now());
                    }
                    return toResponse(notificationRepository.save(notification));
                });
    }

    @Transactional
    public int markAllAsRead(Long userId) {
        return notificationRepository.markAllRead(userId, LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public void replayUnreadNotifications(Long userId, WebSocketSession session) {
        notificationRepository
                .findByRecipient_IdAndReadStatusFalseOrderByCreatedAtDescIdDesc(
                        userId, PageRequest.of(0, MAX_PAGE_SIZE))
                .forEach(notification -> sendNotification(session, toRealtime(notification)));
    }

    private void persistNotification(
            User recipient,
            String type,
            String title,
            String message,
            Long relatedVendorId,
            String href,
            String eventKey
    ) {
        int inserted = notificationRepository.insertIfAbsent(
            recipient.getId(), type, title, message, relatedVendorId, eventKey, href
        );
        if (inserted == 0) {
            return;
        }

        notificationRepository.findByRecipient_IdAndEventKey(recipient.getId(), eventKey)
            .ifPresent(saved -> scheduleDelivery(recipient.getId(), toRealtime(saved)));
    }

    private void scheduleDelivery(Long userId, RealtimeNotification notification) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        deliverNotification(userId, notification);
                    } catch (RuntimeException exception) {
                        log.warn("Realtime delivery failed for user {} and notification {}",
                                userId, notification.id(), exception);
                    }
                }
            });
            return;
        }

        deliverNotification(userId, notification);
    }

    private void deliverNotification(Long userId, RealtimeNotification notification) {
        if (userId == null) {
            return;
        }

        Set<WebSocketSession> sessions = sessionsByUserId.get(userId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        String payload = serialize(notification);

        sessions.removeIf(session -> !session.isOpen());
        sessions.forEach(session -> send(session, payload));
    }

    private NotificationResponse toResponse(UserNotification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getRelatedVendorId(),
                notification.getHref(),
                notification.isReadStatus(),
                notification.getCreatedAt(),
                notification.getReadAt()
        );
    }

    private RealtimeNotification toRealtime(UserNotification notification) {
        return new RealtimeNotification(
                notification.getId().toString(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getCreatedAt().toString(),
                notification.getHref()
        );
    }

    private String serialize(RealtimeNotification notification) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "type", "notification",
                    "notification", notification
            ));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize notification", e);
        }
    }

    private void send(WebSocketSession session, String payload) {
        try {
            synchronized (session) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(payload));
                }
            }
        } catch (IOException e) {
            unregisterSession(session);
        }
    }
}
