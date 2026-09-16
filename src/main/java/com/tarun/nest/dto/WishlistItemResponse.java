package com.tarun.nest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class WishlistItemResponse {

    private Long productId;
    private String name;
    private String imageUrl;
    private BigDecimal price;
    private Integer stock;
}