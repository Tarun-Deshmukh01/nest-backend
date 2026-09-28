package com.tarun.nest.service;

import com.tarun.nest.dto.PaymentRequest;
import com.tarun.nest.dto.PaymentResponse;

public interface PaymentService {

    PaymentResponse processPayment(
            PaymentRequest request,
            Long userId
    );
}