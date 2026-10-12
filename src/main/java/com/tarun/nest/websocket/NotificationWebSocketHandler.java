package com.tarun.nest.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarun.nest.dto.RealtimeNotification;
import com.tarun.nest.dto.VendorResponse;
import com.tarun.nest.service.RealtimeNotificationService;
import com.tarun.nest.service.VendorService;
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
    private final VendorService vendorService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        session.getAttributes().put("authenticated", false);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        if (!"authenticate".equals(payload.path("type").asText())) {
            closeIfUnauthenticated(session);
            return;
        }

        String token = payload.path("token").asText(null);
        Long requestedUserId = parseUserId(payload.path("userId").asText(null));
        String requestedRole = payload.path("role").asText(null);

        if (!isValidAuthentication(token, requestedUserId, requestedRole)) {
            session.close(UNAUTHORIZED);
            return;
        }

        session.getAttributes().put("authenticated", true);
        session.getAttributes().put("userId", requestedUserId);
        session.getAttributes().put("role", requestedRole);

        notificationService.registerSession(requestedUserId, session);

        if ("ADMIN".equalsIgnoreCase(normalizeRole(requestedRole))) {
            sendPendingVendorRequests(session);
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

    private boolean isValidAuthentication(String token, Long requestedUserId, String requestedRole) {
        if (token == null || requestedUserId == null || requestedRole == null) {
            return false;
        }

        if (!jwtUtil.isTokenValid(token)) {
            return false;
        }

        Long tokenUserId = jwtUtil.extractUserId(token);
        String tokenRole = jwtUtil.extractRole(token);

        String normalizedRequestedRole = normalizeRole(requestedRole);
        String normalizedTokenRole = normalizeRole(tokenRole);

        return requestedUserId.equals(tokenUserId)
                && normalizedRequestedRole != null
                && normalizedRequestedRole.equalsIgnoreCase(normalizedTokenRole);
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

    private void sendPendingVendorRequests(WebSocketSession session) {
        try {
            for (VendorResponse vendor : vendorService.getPendingVendors()) {
                String companyName = vendor.getCompanyDetail() == null
                        ? "Vendor"
                        : vendor.getCompanyDetail().getCompanyname();
                if (companyName == null || companyName.isBlank()) {
                    companyName = "Vendor";
                }

                notificationService.sendNotification(session, new RealtimeNotification(
                        "vendor-request-" + vendor.getId(),
                        "New vendor request received",
                        companyName + " submitted a registration request.",
                        java.time.Instant.now().toString(),
                        "/admin/vendor-requests"
                ));
            }
        } catch (RuntimeException exception) {
            log.warn("Unable to replay pending vendor requests to admin websocket session", exception);
        }
    }

    private Long parseUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            return null;
        }

        try {
            return Long.valueOf(userId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void closeIfUnauthenticated(WebSocketSession session) throws Exception {
        if (!Boolean.TRUE.equals(session.getAttributes().get("authenticated"))) {
            session.close(UNAUTHORIZED);
        }
    }
}
