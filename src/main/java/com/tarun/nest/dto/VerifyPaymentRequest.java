package com.tarun.nest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VerifyPaymentRequest(
        @NotNull Long internalOrderId,
        @NotBlank @JsonProperty("razorpay_order_id") String razorpayOrderId,
        @NotBlank @JsonProperty("razorpay_payment_id") String razorpayPaymentId,
        @NotBlank @JsonProperty("razorpay_signature") String razorpaySignature
) {
}
