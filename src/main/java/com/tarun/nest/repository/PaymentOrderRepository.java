package com.tarun.nest.repository;

import com.tarun.nest.entity.PaymentOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentOrderRepository
        extends JpaRepository<PaymentOrder, Long> {

    Optional<PaymentOrder> findByIdAndCustomerEmail(
        Long id, String customerEmail
    );

    Optional<PaymentOrder> findByRazorpayOrderId(
        String razorpayOrderId
    );
}