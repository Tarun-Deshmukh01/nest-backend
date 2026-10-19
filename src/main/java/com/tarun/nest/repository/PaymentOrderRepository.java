package com.tarun.nest.repository;

import com.tarun.nest.entity.PaymentOrder;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentOrderRepository
        extends JpaRepository<PaymentOrder, Long> {

    Optional<PaymentOrder> findByIdAndCustomerEmail(
        Long id, String customerEmail
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PaymentOrder p "
            + "where p.id = :id and p.customerEmail = :customerEmail")
    Optional<PaymentOrder> findByIdAndCustomerEmailForUpdate(
            @Param("id") Long id,
            @Param("customerEmail") String customerEmail
    );

    Optional<PaymentOrder> findByRazorpayOrderId(
        String razorpayOrderId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PaymentOrder p where p.razorpayOrderId = :razorpayOrderId")
    Optional<PaymentOrder> findByRazorpayOrderIdForUpdate(
            @Param("razorpayOrderId") String razorpayOrderId
    );

    Optional<PaymentOrder> findFirstByOrderIdOrderByIdDesc(Long orderId);

    List<PaymentOrder> findByStatusAndCreatedAtBefore(
            PaymentOrder.PaymentStatus status,
            java.time.LocalDateTime createdAt
    );

    @Modifying
    @Query("update PaymentOrder p set p.deliveryAddress = null "
            + "where p.deliveryAddress.id = :addressId")
    int clearDeliveryAddressReferences(@Param("addressId") Long addressId);
}