package com.tarun.nest.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "payment_orders",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "razorpay_order_id")
    }
)
public class PaymentOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String customerEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column
    private Long productId;

    @Column
    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_address_id")
    private CustomerAddress deliveryAddress;

    // Always store money in the smallest currency unit (paise).
    @Column(nullable = false)
    private Long amountPaise;

    @Column(nullable = false, length = 3)
    private String currency = "INR";

    @Column(unique = true)
    private String razorpayOrderId;

    private String razorpayPaymentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.CREATED;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public enum PaymentStatus {
        CREATING,
        CREATED,
        VERIFIED,
        PAID,
        FAILED
    }

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String value) { customerEmail = value; }
    public Order getOrder() { return order; }
    public void setOrder(Order value) { order = value; }
    public Long getProductId() { return productId; }
    public void setProductId(Long value) { productId = value; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer value) { quantity = value; }
    public CustomerAddress getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(CustomerAddress value) {
        deliveryAddress = value;
    }
    public Long getAmountPaise() { return amountPaise; }
    public void setAmountPaise(Long value) { amountPaise = value; }
    public String getCurrency() { return currency; }
    public String getRazorpayOrderId() { return razorpayOrderId; }
    public void setRazorpayOrderId(String value) {
        razorpayOrderId = value;
    }
    public String getRazorpayPaymentId() { return razorpayPaymentId; }
    public void setRazorpayPaymentId(String value) {
        razorpayPaymentId = value;
    }
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus value) { status = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}