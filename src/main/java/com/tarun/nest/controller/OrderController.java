package com.tarun.nest.controller;

import com.tarun.nest.dto.CreateOrderRequest;
import com.tarun.nest.dto.OrderResponse;
import com.tarun.nest.service.OrderService;
import com.tarun.nest.util.AuthorizationUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final AuthorizationUtil authorizationUtil;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            Authentication authentication
    ) {

        Long userId =
                authorizationUtil.getUserId(authentication);

        OrderResponse response =
                orderService.createOrder(request, userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}