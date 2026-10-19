package com.tarun.nest.service;

import com.tarun.nest.dto.CreateOrderRequest;
import com.tarun.nest.dto.OrderResponse;
import com.tarun.nest.enums.OrderStatus;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(
            CreateOrderRequest request,
            Long userId,
            String idempotencyKey
    );

    List<OrderResponse> getMyOrders(Long userId);

    OrderResponse getMyOrder(Long orderId, Long userId);

    List<OrderResponse> getVendorOrders(Long vendorUserId);

    OrderResponse getVendorOrder(Long orderId, Long vendorUserId);

    OrderResponse updateVendorOrderStatus(
            Long orderId,
            Long vendorUserId,
            OrderStatus status
    );
}