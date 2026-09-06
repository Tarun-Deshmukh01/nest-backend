package com.tarun.nest.controller;

import com.tarun.nest.dto.AddToCartRequest;
import com.tarun.nest.dto.AddToCartResponse;
import com.tarun.nest.entity.CartItem;
import com.tarun.nest.service.CartService;
import com.tarun.nest.util.AuthorizationUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final AuthorizationUtil authorizationUtil;

    @PostMapping("/items")
    public ResponseEntity<AddToCartResponse> addToCart(
            @Valid @RequestBody AddToCartRequest request,
            Authentication authentication
    ) {

        Long userId = authorizationUtil.getUserId(authentication);

        if (userId == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        CartItem cartItem = cartService.addToCart(userId, request);

        AddToCartResponse response = new AddToCartResponse(
                cartItem.getId(),
                cartItem.getProduct().getId(),
                cartItem.getProduct().getName(),
                cartItem.getProduct().getPrice(),
                cartItem.getQuantity()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}