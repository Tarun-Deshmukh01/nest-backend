package com.tarun.nest.service;

import com.tarun.nest.dto.CreatePaymentRequest;
import com.tarun.nest.dto.CreatePaymentResponse;
import com.tarun.nest.dto.PaymentVerificationResponse;
import com.tarun.nest.dto.VerifyPaymentRequest;

public interface PaymentService {

    CreatePaymentResponse createOrder(
            CreatePaymentRequest request,
            String customerEmail
    ) throws Exception;

    PaymentVerificationResponse verifyPayment(
            VerifyPaymentRequest request,
            String customerEmail
    ) throws Exception;
}
