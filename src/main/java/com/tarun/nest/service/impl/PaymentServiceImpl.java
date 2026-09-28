package com.tarun.nest.service.impl;

import com.tarun.nest.dto.PaymentRequest;
import com.tarun.nest.dto.PaymentResponse;
import com.tarun.nest.entity.Payment;
import com.tarun.nest.enums.PaymentStatus;
import com.tarun.nest.repository.PaymentRepository;
import com.tarun.nest.service.PaymentService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    @Override
    public PaymentResponse processPayment(
            PaymentRequest request,
            Long userId
    ) {

        /*
         * Dummy payment processing.
         *
         * In a real implementation this is where
         * Razorpay/Stripe/etc. would be called.
         */

        String transactionId =
                "DUMMY-" + UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase();

        Payment payment = Payment.builder()
                .userId(userId)
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .transactionId(transactionId)
                .status(PaymentStatus.SUCCESS)
                .createdAt(LocalDateTime.now())
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        return PaymentResponse.builder()
                .paymentId(savedPayment.getId())
                .orderId(savedPayment.getOrderId())
                .amount(savedPayment.getAmount())
                .status(savedPayment.getStatus())
                .paymentMethod(savedPayment.getPaymentMethod())
                .transactionId(savedPayment.getTransactionId())
                .message("Dummy payment successful")
                .build();
    }
}