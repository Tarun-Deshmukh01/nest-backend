package com.tarun.nest.dto;

public record RealtimeNotification(
        String id,
        String title,
        String message,
        String createdAt,
        String href
) {
}
