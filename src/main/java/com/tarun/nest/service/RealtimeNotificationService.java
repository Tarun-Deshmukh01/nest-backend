package com.tarun.nest.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarun.nest.dto.RealtimeNotification;
import com.tarun.nest.entity.Role;
import com.tarun.nest.entity.User;
import com.tarun.nest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
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
    private final Map<Long, Set<WebSocketSession>> sessionsByUserId = new ConcurrentHashMap<>();

    public void registerSession(Long userId, WebSocketSession session) {
        sessionsByUserId.computeIfAbsent(userId, ignored -> ConcurrentHashMap.newKeySet()).add(session);
    }

    public void unregisterSession(WebSocketSession session) {
        sessionsByUserId.values().forEach(sessions -> sessions.remove(session));
    }

    public void sendNotification(Long userId, String title, String message, String href) {
        sendNotification(userId, createNotification(UUID.randomUUID().toString(), title, message, href));
    }

    public void sendNotification(WebSocketSession session, RealtimeNotification notification) {
        send(session, serialize(notification));
    }

    public void sendNotificationToUsers(Collection<Long> userIds, String title, String message, String href) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }

        RealtimeNotification notification = new RealtimeNotification(
                UUID.randomUUID().toString(),
                title,
                message,
                Instant.now().toString(),
                href
        );

        userIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .forEach(userId -> sendNotification(userId, notification));
    }

    public void sendNotificationToAdmins(String title, String message, String href) {
        sendNotificationToAdmins(UUID.randomUUID().toString(), title, message, href);
    }

    public void sendNotificationToAdmins(String notificationId, String title, String message, String href) {
        var adminUsers = userRepository.findByRole(Role.ADMIN);

        if (adminUsers.isEmpty()) {
            log.warn("No admin users found for notification: {} - {}", title, message);
            return;
        }

        adminUsers.stream()
                .map(User::getId)
                .filter(Objects::nonNull)
                .distinct()
            .forEach(adminUserId -> sendNotification(
                adminUserId,
                createNotification(notificationId, title, message, href)
            ));
    }

    public void sendNotificationToRole(Role role, String title, String message, String href) {
        userRepository.findByRole(role).stream()
                .map(User::getId)
                .filter(Objects::nonNull)
                .distinct()
                .forEach(userId -> sendNotification(userId, title, message, href));
    }

    public void sendNotification(Long userId, RealtimeNotification notification) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    deliverNotification(userId, notification);
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
            log.warn("Notification skipped for user {} because no active websocket session is registered: {} - {}",
                    userId, notification.title(), notification.message());
            return;
        }

        String payload = serialize(notification);

        sessions.removeIf(session -> !session.isOpen());
        sessions.forEach(session -> send(session, payload));
    }

    private RealtimeNotification createNotification(String id, String title, String message, String href) {
        return new RealtimeNotification(id, title, message, Instant.now().toString(), href);
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
