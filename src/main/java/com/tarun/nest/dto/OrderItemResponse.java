package com.tarun.nest.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class OrderItemResponse {

    private Long productId;

    private String productName;

    private String productImage;

    private Long vendorId;

    private String vendorName;

    private Integer quantity;

    private BigDecimal price;

    private BigDecimal subtotal;
}