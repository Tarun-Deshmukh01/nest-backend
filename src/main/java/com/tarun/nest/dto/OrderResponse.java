package com.tarun.nest.dto;

import com.tarun.nest.enums.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class OrderResponse {

    private Long orderId;

    private BigDecimal totalAmount;

    private OrderStatus status;

    private String address;

    private String city;

    private String state;

    private String pincode;

    private String country;

    private LocalDateTime createdAt;

    private List<OrderItemResponse> items;
}