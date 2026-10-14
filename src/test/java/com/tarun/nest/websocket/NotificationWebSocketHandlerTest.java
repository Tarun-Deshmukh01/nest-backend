package com.tarun.nest.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarun.nest.entity.Role;
import com.tarun.nest.entity.User;
import com.tarun.nest.repository.UserRepository;
import com.tarun.nest.service.RealtimeNotificationService;
import com.tarun.nest.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationWebSocketHandlerTest {

    @Test
    void authenticate_acceptsRoleWithRolePrefix() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JwtUtil jwtUtil = mock(JwtUtil.class);
        RealtimeNotificationService notificationService = mock(RealtimeNotificationService.class);
        UserRepository userRepository = mock(UserRepository.class);
        NotificationWebSocketHandler handler = new NotificationWebSocketHandler(
            objectMapper,
            jwtUtil,
            notificationService,
            userRepository
        );

        WebSocketSession session = mock(WebSocketSession.class);
        Map<String, Object> attributes = new HashMap<>();
        when(session.getAttributes()).thenReturn(attributes);
        when(jwtUtil.isTokenValid("token-123")).thenReturn(true);
        when(jwtUtil.extractUserId("token-123")).thenReturn(10L);
        when(jwtUtil.extractRole("token-123")).thenReturn("ADMIN");
        User admin = new User();
        admin.setId(10L);
        admin.setRole(Role.ADMIN);
        when(userRepository.findById(10L)).thenReturn(Optional.of(admin));

        handler.handleTextMessage(session, new TextMessage("{\"type\":\"authenticate\",\"token\":\"token-123\",\"userId\":\"999\",\"role\":\"ROLE_VENDOR\"}"));

        verify(notificationService).registerSession(eq(10L), same(session));
        verify(notificationService).replayUnreadNotifications(eq(10L), same(session));
    }

    @Test
    void invalidJwtIsRejected() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JwtUtil jwtUtil = mock(JwtUtil.class);
        RealtimeNotificationService notificationService = mock(RealtimeNotificationService.class);
        UserRepository userRepository = mock(UserRepository.class);
        NotificationWebSocketHandler handler = new NotificationWebSocketHandler(
            objectMapper,
            jwtUtil,
            notificationService,
            userRepository
        );

        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getAttributes()).thenReturn(new HashMap<>());
        when(jwtUtil.isTokenValid("expired-token")).thenReturn(false);

        handler.handleTextMessage(session, new TextMessage(
            "{\"type\":\"authenticate\",\"token\":\"expired-token\"}"
        ));

        verify(session).close(any());
        verify(notificationService, never()).registerSession(eq(10L), same(session));
    }

    @Test
    void databaseRoleMustMatchJwtRole() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JwtUtil jwtUtil = mock(JwtUtil.class);
        RealtimeNotificationService notificationService = mock(RealtimeNotificationService.class);
        UserRepository userRepository = mock(UserRepository.class);
        NotificationWebSocketHandler handler = new NotificationWebSocketHandler(
                objectMapper, jwtUtil, notificationService, userRepository
        );
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getAttributes()).thenReturn(new HashMap<>());
        when(jwtUtil.isTokenValid("vendor-token")).thenReturn(true);
        when(jwtUtil.extractUserId("vendor-token")).thenReturn(21L);
        when(jwtUtil.extractRole("vendor-token")).thenReturn("VENDOR");
        User user = new User();
        user.setId(21L);
        user.setRole(Role.VENDOR);
        when(userRepository.findById(21L)).thenReturn(Optional.of(user));

        handler.handleTextMessage(session, new TextMessage(
                "{\"type\":\"authenticate\",\"token\":\"vendor-token\",\"userId\":\"10\",\"role\":\"ADMIN\"}"
        ));

        verify(notificationService).registerSession(21L, session);
        verify(notificationService).replayUnreadNotifications(21L, session);
    }
}
