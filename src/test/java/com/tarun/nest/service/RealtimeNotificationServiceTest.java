package com.tarun.nest.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarun.nest.entity.Role;
import com.tarun.nest.entity.User;
import com.tarun.nest.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RealtimeNotificationServiceTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RealtimeNotificationService notificationService;

    @Test
    void sendNotificationToAdmins_sendsToAllAdminUsers() {
        User admin = new User();
        admin.setId(10L);
        admin.setRole(Role.ADMIN);

        when(userRepository.findByRole(Role.ADMIN)).thenReturn(List.of(admin));

        notificationService.sendNotificationToAdmins(
                "New vendor request",
                "A vendor has submitted a registration request.",
                "/admin/vendors"
        );

        verify(userRepository).findByRole(Role.ADMIN);
    }

    @Test
    void sendNotificationToUsers_sendsToSpecifiedUserIds() {
        notificationService.sendNotificationToUsers(
                Set.of(21L, 22L),
                "Order update",
                "Your products have received a new customer order.",
                "/vendor/orders"
        );
    }
}
