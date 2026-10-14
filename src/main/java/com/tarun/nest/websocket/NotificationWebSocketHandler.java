package com.tarun.nest.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarun.nest.entity.User;
import com.tarun.nest.repository.UserRepository;
import com.tarun.nest.service.RealtimeNotificationService;
import com.tarun.nest.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private static final CloseStatus UNAUTHORIZED = new CloseStatus(4001, "Unauthorized");

    private final ObjectMapper objectMapper;
    private final JwtUtil jwtUtil;
    private final RealtimeNotificationService notificationService;
    private final UserRepository userRepository;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        session.getAttributes().put("authenticated", false);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonNode payload;
        try {
            payload = objectMapper.readTree(message.getPayload());
        } catch (Exception exception) {
            session.close(UNAUTHORIZED);
            return;
        }

        if (Boolean.TRUE.equals(session.getAttributes().get("authenticated"))) {
            session.close(UNAUTHORIZED);
            return;
        }

        if (!"authenticate".equals(payload.path("type").asText())) {
            session.close(UNAUTHORIZED);
            return;
        }

        String token = payload.path("token").asText(null);
        User authenticatedUser = resolveAuthenticatedUser(token);
        if (authenticatedUser == null) {
            session.close(UNAUTHORIZED);
            return;
        }

        session.getAttributes().put("authenticated", true);
        session.getAttributes().put("userId", authenticatedUser.getId());
        session.getAttributes().put("role", authenticatedUser.getRole().name());

        notificationService.registerSession(authenticatedUser.getId(), session);
        try {
            notificationService.replayUnreadNotifications(authenticatedUser.getId(), session);
        } catch (RuntimeException exception) {
            log.warn("Unable to replay notifications for authenticated user {}", authenticatedUser.getId(), exception);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        notificationService.unregisterSession(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        notificationService.unregisterSession(session);
    }

    private User resolveAuthenticatedUser(String token) {
        if (token == null || !jwtUtil.isTokenValid(token)) {
            return null;
        }

        Long tokenUserId = jwtUtil.extractUserId(token);
        String tokenRole = normalizeRole(jwtUtil.extractRole(token));
        if (tokenUserId == null || tokenRole == null) {
            return null;
        }

        return userRepository.findById(tokenUserId)
                .filter(user -> Boolean.TRUE.equals(user.getActive()))
                .filter(user -> user.getRole() != null && user.getRole().name().equalsIgnoreCase(tokenRole))
                .orElse(null);
    }

    private String normalizeRole(String role) {
        if (role == null) {
            return null;
        }

        String normalized = role.trim();
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring("ROLE_".length());
        }

        return normalized;
    }

}
