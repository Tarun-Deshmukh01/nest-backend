package com.tarun.nest.dto;

public record PaymentVerificationResponse(
        boolean success,
        String status,
        String message
) {
}
