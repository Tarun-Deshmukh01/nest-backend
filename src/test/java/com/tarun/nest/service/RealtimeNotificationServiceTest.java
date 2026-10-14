package com.tarun.nest.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarun.nest.entity.Role;
import com.tarun.nest.entity.User;
import com.tarun.nest.entity.UserNotification;
import com.tarun.nest.repository.UserNotificationRepository;
import com.tarun.nest.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RealtimeNotificationServiceTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserNotificationRepository notificationRepository;

    @InjectMocks
    private RealtimeNotificationService notificationService;

    @Test
    void sendNotificationToAdmins_sendsToAllAdminUsers() {
        User admin = new User();
        admin.setId(10L);
        admin.setRole(Role.ADMIN);

        when(userRepository.findByRoleAndActiveTrue(Role.ADMIN)).thenReturn(List.of(admin));
        when(notificationRepository.insertIfAbsent(
            eq(10L), anyString(), anyString(), anyString(), any(), anyString(), anyString()
        )).thenReturn(1);
        when(notificationRepository.findByRecipient_IdAndEventKey(eq(10L), anyString()))
            .thenReturn(Optional.of(notification(100L)));

        notificationService.sendNotificationToAdmins(
                "New vendor request",
                "A vendor has submitted a registration request.",
                "/admin/vendors"
        );

        verify(userRepository).findByRoleAndActiveTrue(Role.ADMIN);
        verify(notificationRepository).insertIfAbsent(
            eq(10L), anyString(), anyString(), anyString(), any(), anyString(), anyString()
        );
    }

    @Test
    void vendorApplicationNotificationsAreIdempotentPerAdmin() {
        User admin = new User();
        admin.setId(10L);
        admin.setRole(Role.ADMIN);
        when(userRepository.findByRoleAndActiveTrue(Role.ADMIN)).thenReturn(List.of(admin));
        when(notificationRepository.insertIfAbsent(
            10L, "VENDOR_APPLICATION", "New vendor request received",
            "Example Shop submitted a registration request.", 77L,
            "vendor-application:77", "/admin/vendor-requests"
        )).thenReturn(0);

        notificationService.notifyAdminsOfVendorApplication(77L, "Example Shop");

        verify(notificationRepository).insertIfAbsent(
            10L, "VENDOR_APPLICATION", "New vendor request received",
            "Example Shop submitted a registration request.", 77L,
            "vendor-application:77", "/admin/vendor-requests"
        );
    }

    @Test
    void notificationQueriesAreScopedToTheRecipient() {
        when(notificationRepository.countByRecipient_IdAndReadStatusFalse(21L)).thenReturn(3L);
        when(notificationRepository.findByIdAndRecipient_Id(8L, 21L)).thenReturn(Optional.empty());

        Assertions.assertEquals(3L, notificationService.getUnreadCount(21L));
        Assertions.assertTrue(notificationService.markAsRead(8L, 21L).isEmpty());

        verify(notificationRepository).findByIdAndRecipient_Id(8L, 21L);
    }

    @Test
    void markAsReadAndReadAllUpdateUnreadState() {
        UserNotification notification = notification(8L);
        when(notificationRepository.findByIdAndRecipient_Id(8L, 21L))
                .thenReturn(Optional.of(notification));
        when(notificationRepository.save(notification)).thenReturn(notification);
        when(notificationRepository.markAllRead(eq(21L), any(LocalDateTime.class))).thenReturn(2);

        var updated = notificationService.markAsRead(8L, 21L);
        int updatedCount = notificationService.markAllAsRead(21L);

        Assertions.assertTrue(updated.isPresent());
        Assertions.assertTrue(updated.get().read());
        Assertions.assertNotNull(updated.get().readAt());
        Assertions.assertEquals(2, updatedCount);
    }

    @Test
    void sendNotificationToUsersPersistsForSpecifiedUsers() {
        User vendor = new User();
        vendor.setId(21L);
        when(userRepository.findAllById(anyList())).thenReturn(List.of(vendor));
        when(notificationRepository.insertIfAbsent(
            eq(21L), anyString(), anyString(), anyString(), any(), anyString(), anyString()
        )).thenReturn(1);
        when(notificationRepository.findByRecipient_IdAndEventKey(eq(21L), anyString()))
            .thenReturn(Optional.of(notification(101L)));

        notificationService.sendNotificationToUsers(
                Set.of(21L, 22L),
                "Order update",
                "Your products have received a new customer order.",
                "/vendor/orders"
        );

        verify(notificationRepository).insertIfAbsent(
            eq(21L), anyString(), anyString(), anyString(), any(), anyString(), anyString()
        );
    }

    @Test
    void notificationIsNotDeliveredBeforeTransactionCommit() throws Exception {
        User admin = new User();
        admin.setId(10L);
        admin.setRole(Role.ADMIN);
        when(userRepository.findByRoleAndActiveTrue(Role.ADMIN)).thenReturn(List.of(admin));
        when(notificationRepository.insertIfAbsent(
                eq(10L), anyString(), anyString(), anyString(), any(), anyString(), anyString()
        )).thenReturn(1);
        when(notificationRepository.findByRecipient_IdAndEventKey(eq(10L), anyString()))
                .thenReturn(Optional.of(notification(102L)));
        WebSocketSession session = org.mockito.Mockito.mock(WebSocketSession.class);
        notificationService.registerSession(10L, session);

        org.springframework.transaction.support.TransactionSynchronizationManager.initSynchronization();
        try {
            notificationService.sendNotificationToAdmins("Vendor request", "Submitted", "/admin/vendors");
            verifyNoInteractions(session);
        } finally {
            org.springframework.transaction.support.TransactionSynchronizationManager.clearSynchronization();
        }
        verify(objectMapper, never()).writeValueAsString(any());
        }

        @Test
        void websocketFailureDoesNotThrowOrUndoPersistence() throws Exception {
        User recipient = new User();
        recipient.setId(21L);
        when(userRepository.findAllById(anyList())).thenReturn(List.of(recipient));
        when(notificationRepository.insertIfAbsent(
            eq(21L), anyString(), anyString(), anyString(), any(), anyString(), anyString()
        )).thenReturn(1);
        when(notificationRepository.findByRecipient_IdAndEventKey(eq(21L), anyString()))
            .thenReturn(Optional.of(notification(103L)));
        WebSocketSession session = org.mockito.Mockito.mock(WebSocketSession.class);
        when(session.isOpen()).thenReturn(true);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        doThrow(new IOException("socket closed")).when(session).sendMessage(any(TextMessage.class));
        notificationService.registerSession(21L, session);

        Assertions.assertDoesNotThrow(() -> notificationService.sendNotificationToUsers(
            Set.of(21L), "Order update", "Order placed", "/vendor/orders"
        ));

        verify(notificationRepository).insertIfAbsent(
            eq(21L), anyString(), anyString(), anyString(), any(), anyString(), anyString()
        );
    }

    private UserNotification notification(Long id) {
        UserNotification notification = new UserNotification();
        notification.setId(id);
        notification.setTitle("New vendor request received");
        notification.setMessage("Example Shop submitted a registration request.");
        notification.setCreatedAt(LocalDateTime.now());
        notification.setHref("/admin/vendor-requests");
        return notification;
    }
}
