package com.tarun.nest.controller;

import com.tarun.nest.dto.*;
import com.tarun.nest.service.PaymentService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/orders")
    public ResponseEntity<CreatePaymentResponse> createOrder(
            @Valid @RequestBody CreatePaymentRequest request,
            Authentication authentication
    ) throws Exception {

        String customerEmail = authentication.getName();

        return ResponseEntity.ok(
            paymentService.createOrder(request, customerEmail)
        );
    }

    @PostMapping("/verify")
    public ResponseEntity<PaymentVerificationResponse> verifyPayment(
            @Valid @RequestBody VerifyPaymentRequest request,
            Authentication authentication
    ) throws Exception {

        String customerEmail = authentication.getName();

        return ResponseEntity.ok(
            paymentService.verifyPayment(request, customerEmail)
        );
    }
}