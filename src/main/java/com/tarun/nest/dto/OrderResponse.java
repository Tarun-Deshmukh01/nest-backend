package com.tarun.nest.dto;

import com.tarun.nest.enums.OrderStatus;
import com.tarun.nest.enums.OrderPaymentStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class OrderResponse {

    private Long orderId;

    private String orderNumber;

    private BigDecimal totalAmount;

    private BigDecimal subtotal;

    private BigDecimal deliveryCharges;

    private BigDecimal discountAmount;

    private String currency;

    private OrderStatus status;

    private OrderPaymentStatus paymentStatus;

    private String recipientName;

    private String phoneNumber;

    private String address;

    private String addressLine2;

    private String landmark;

    private String city;

    private String state;

    private String pincode;

    private String country;

    private LocalDateTime createdAt;

    private List<OrderItemResponse> items;
}