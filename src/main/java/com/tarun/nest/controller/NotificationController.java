package com.tarun.nest.controller;

import com.tarun.nest.dto.NotificationResponse;
import com.tarun.nest.dto.ReadAllResponse;
import com.tarun.nest.dto.UnreadCountResponse;
import com.tarun.nest.security.JwtAuthenticationDetails;
import com.tarun.nest.service.RealtimeNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final RealtimeNotificationService notificationService;

    @GetMapping
    public Page<NotificationResponse> getNotifications(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return notificationService.getNotifications(
                userId(authentication),
                PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100))
        );
    }

    @GetMapping("/unread-count")
    public UnreadCountResponse getUnreadCount(Authentication authentication) {
        return new UnreadCountResponse(notificationService.getUnreadCount(userId(authentication)));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return notificationService.markAsRead(id, userId(authentication))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PatchMapping("/read-all")
    public ReadAllResponse markAllAsRead(Authentication authentication) {
        return new ReadAllResponse(notificationService.markAllAsRead(userId(authentication)));
    }

    private Long userId(Authentication authentication) {
        if (authentication == null || !(authentication.getDetails() instanceof JwtAuthenticationDetails details)
                || details.getUserId() == null) {
            throw new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException(
                    "Authenticated user identity is unavailable"
            );
        }
        return details.getUserId();
    }
}