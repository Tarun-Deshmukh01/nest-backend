package com.tarun.nest.service;

import com.tarun.nest.dto.CreatePaymentRequest;
import com.tarun.nest.dto.CreateOrderRequest;
import com.tarun.nest.dto.CreatePaymentResponse;
import com.tarun.nest.dto.PaymentVerificationResponse;
import com.tarun.nest.dto.VerifyPaymentRequest;

public interface PaymentService {

    CreatePaymentResponse createOrder(
            CreatePaymentRequest request,
            String customerEmail
    ) throws Exception;

    CreatePaymentResponse createCartPayment(
            CreateOrderRequest request,
            String customerEmail,
            String idempotencyKey
    ) throws Exception;

    CreatePaymentResponse createPaymentForOrder(
            Long orderId,
            String customerEmail
    ) throws Exception;

    PaymentVerificationResponse verifyPayment(
            VerifyPaymentRequest request,
            String customerEmail
    ) throws Exception;

    void handleWebhook(String payload, String signature) throws Exception;
}
