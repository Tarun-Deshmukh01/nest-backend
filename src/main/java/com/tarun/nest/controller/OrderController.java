package com.tarun.nest.controller;

import com.tarun.nest.dto.CreateOrderRequest;
import com.tarun.nest.dto.OrderResponse;
import com.tarun.nest.dto.CreatePaymentResponse;
import com.tarun.nest.service.PaymentService;
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
    private final PaymentService paymentService;
    private final AuthorizationUtil authorizationUtil;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            Authentication authentication,
            @RequestHeader("Idempotency-Key") String idempotencyKey
    ) {

        Long userId =
                authorizationUtil.getUserId(authentication);

        OrderResponse response =
                orderService.createOrder(request, userId, idempotencyKey);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/my")
    public ResponseEntity<java.util.List<OrderResponse>> getMyOrders(
            Authentication authentication) {
        return ResponseEntity.ok(orderService.getMyOrders(
                authorizationUtil.getUserId(authentication)
        ));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getMyOrder(
            @PathVariable Long orderId,
            Authentication authentication) {
        return ResponseEntity.ok(orderService.getMyOrder(
                orderId, authorizationUtil.getUserId(authentication)
        ));
    }

    @PostMapping("/{orderId}/payment")
    public ResponseEntity<CreatePaymentResponse> initiatePayment(
            @PathVariable Long orderId,
            Authentication authentication) throws Exception {
        return ResponseEntity.ok(paymentService.createPaymentForOrder(
                orderId, authentication.getName()
        ));
    }
}