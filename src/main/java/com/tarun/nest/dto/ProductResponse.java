package com.tarun.nest.dto;

import com.tarun.nest.entity.ProductCategory;
import com.tarun.nest.entity.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ProductResponse {

    private Long id;
    private String name;
    private String imageUrl;
    private ProductCategory category;
    private ProductStatus status;
    private BigDecimal price;
    private Integer stock;
}