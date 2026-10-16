package com.tarun.nest.dto;

public record CreatePaymentResponse(
        Long internalOrderId,
        String razorpayOrderId,
        Long amount,
        String currency,
        String keyId,
        String productName
) {
}
