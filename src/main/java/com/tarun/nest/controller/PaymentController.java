package com.tarun.nest.controller;

import com.tarun.nest.dto.PaymentRequest;
import com.tarun.nest.dto.PaymentResponse;
import com.tarun.nest.service.PaymentService;
import com.tarun.nest.util.AuthorizationUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final AuthorizationUtil authorizationUtil;

    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(
            @Valid @RequestBody PaymentRequest request,
            Authentication authentication
    ) {

        Long userId =
                authorizationUtil.getUserId(authentication);

        PaymentResponse response =
                paymentService.processPayment(request, userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}