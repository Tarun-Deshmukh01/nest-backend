package com.tarun.nest.service;

import com.tarun.nest.dto.CreateOrderRequest;
import com.tarun.nest.dto.OrderResponse;

public interface OrderService {

    OrderResponse createOrder(
            CreateOrderRequest request,
            Long userId
    );
}