package com.tarun.nest.dto;

import java.math.BigDecimal;

public record AddToCartResponse(
        Long cartItemId,
        Long productId,
        String productName,
        BigDecimal price,
        Integer quantity
) {
}