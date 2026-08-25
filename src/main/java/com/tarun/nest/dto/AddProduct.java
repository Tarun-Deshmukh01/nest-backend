package com.tarun.nest.dto;

import com.tarun.nest.entity.ProductCategory;
import com.tarun.nest.entity.ProductStatus;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Data
public class AddProduct {

    private String name;

    private MultipartFile image;

    private ProductCategory category;

    private ProductStatus status = ProductStatus.PUBLISHED;

    private BigDecimal price;

    private Integer stock;
}