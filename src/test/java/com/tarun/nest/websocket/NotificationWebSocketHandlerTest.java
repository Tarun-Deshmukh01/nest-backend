package com.tarun.nest.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarun.nest.dto.VendorResponse;
import com.tarun.nest.service.RealtimeNotificationService;
import com.tarun.nest.service.VendorService;
import com.tarun.nest.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationWebSocketHandlerTest {

    @Test
    void authenticate_acceptsRoleWithRolePrefix() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JwtUtil jwtUtil = mock(JwtUtil.class);
        RealtimeNotificationService notificationService = mock(RealtimeNotificationService.class);
        VendorService vendorService = mock(VendorService.class);
        NotificationWebSocketHandler handler = new NotificationWebSocketHandler(
            objectMapper,
            jwtUtil,
            notificationService,
            vendorService
        );

        WebSocketSession session = mock(WebSocketSession.class);
        Map<String, Object> attributes = new HashMap<>();
        when(session.getAttributes()).thenReturn(attributes);
        when(jwtUtil.isTokenValid("token-123")).thenReturn(true);
        when(jwtUtil.extractUserId("token-123")).thenReturn(10L);
        when(jwtUtil.extractRole("token-123")).thenReturn("ADMIN");

        handler.handleTextMessage(session, new TextMessage("{\"type\":\"authenticate\",\"token\":\"token-123\",\"userId\":\"10\",\"role\":\"ROLE_ADMIN\"}"));

        verify(notificationService).registerSession(eq(10L), same(session));
    }

        @Test
        void authenticate_adminReceivesPendingVendorRequests() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JwtUtil jwtUtil = mock(JwtUtil.class);
        RealtimeNotificationService notificationService = mock(RealtimeNotificationService.class);
        VendorService vendorService = mock(VendorService.class);
        NotificationWebSocketHandler handler = new NotificationWebSocketHandler(
            objectMapper,
            jwtUtil,
            notificationService,
            vendorService
        );

        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getAttributes()).thenReturn(new HashMap<>());
        when(jwtUtil.isTokenValid("admin-token")).thenReturn(true);
        when(jwtUtil.extractUserId("admin-token")).thenReturn(10L);
        when(jwtUtil.extractRole("admin-token")).thenReturn("ADMIN");
        when(vendorService.getPendingVendors()).thenReturn(List.of(
            new VendorResponse(
                13L,
                new VendorResponse.CompanyDetail("QA Market", "2026", "Test shop"),
                null,
                null,
                "PENDING"
            )
        ));

        handler.handleTextMessage(session, new TextMessage(
            "{\"type\":\"authenticate\",\"token\":\"admin-token\",\"userId\":\"10\",\"role\":\"ROLE_ADMIN\"}"
        ));

        verify(notificationService).sendNotification(
            same(session),
            argThat(notification ->
                notification.id().equals("vendor-request-13")
                    && notification.title().equals("New vendor request received")
                    && notification.href().equals("/admin/vendor-requests"))
        );
        }
}
