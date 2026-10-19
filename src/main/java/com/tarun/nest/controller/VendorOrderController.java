package com.tarun.nest.controller;

import com.tarun.nest.dto.OrderResponse;
import com.tarun.nest.dto.UpdateOrderStatusRequest;
import com.tarun.nest.security.JwtAuthenticationDetails;
import com.tarun.nest.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vendor/orders")
@RequiredArgsConstructor
public class VendorOrderController {

    private final OrderService orderService;

    @GetMapping
    public List<OrderResponse> getOrders(Authentication authentication) {
        return orderService.getVendorOrders(userId(authentication));
    }

    @GetMapping("/{orderId}")
    public OrderResponse getOrder(
            @PathVariable Long orderId,
            Authentication authentication) {
        return orderService.getVendorOrder(orderId, userId(authentication));
    }

    @PatchMapping("/{orderId}/status")
    public OrderResponse updateStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request,
            Authentication authentication) {
        return orderService.updateVendorOrderStatus(
                orderId, userId(authentication), request.status()
        );
    }

    private Long userId(Authentication authentication) {
        if (authentication.getDetails() instanceof JwtAuthenticationDetails details
                && details.getUserId() != null) {
            return details.getUserId();
        }
        throw new org.springframework.security.authentication
                .AuthenticationCredentialsNotFoundException(
                        "Authenticated vendor identity is unavailable"
                );
    }
}
