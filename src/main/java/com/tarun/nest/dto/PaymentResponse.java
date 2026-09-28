package com.tarun.nest.dto;

import com.tarun.nest.enums.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class PaymentResponse {

    private Long paymentId;

    private Long orderId;

    private BigDecimal amount;

    private PaymentStatus status;

    private String paymentMethod;

    private String transactionId;

    private String message;
}