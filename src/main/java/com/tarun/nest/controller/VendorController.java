package com.tarun.nest.controller;

import com.tarun.nest.dto.AddProduct;
import com.tarun.nest.dto.ApiResponse;
import com.tarun.nest.entity.Product;
import com.tarun.nest.entity.User;
import com.tarun.nest.repository.UserRepository;
import com.tarun.nest.security.JwtAuthenticationDetails;
import com.tarun.nest.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import com.tarun.nest.dto.ProductResponse;

import java.util.List;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/vendor")
@RequiredArgsConstructor
@Slf4j
public class VendorController {

    private final ProductService productService;
    private final UserRepository userRepository;

    @PostMapping(
            value = "/products",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<ApiResponse> addProduct(
            @ModelAttribute AddProduct productData,
            Authentication authentication) {

        try {
            log.info(
                    "Vendor {} adding new product: {}",
                    authentication.getName(),
                    productData.getName()
            );

            JwtAuthenticationDetails details =
                    (JwtAuthenticationDetails) authentication.getDetails();

            User vendor = userRepository
                    .findById(details.getUserId())
                    .orElseThrow(() ->
                            new RuntimeException("Vendor not found")
                    );

            Product product =
                    productService.addProduct(productData, vendor);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new ApiResponse(
                            HttpStatus.CREATED.value(),
                            "Product added successfully",
                            product
                    ));

        } catch (IOException e) {

            log.error("Failed to save product image", e);

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Failed to upload product image",
                            null
                    ));
        }
    }
    @GetMapping("/products")
    public ResponseEntity<ApiResponse> getProducts(
            Authentication authentication) {

        log.info(
                "Fetching products for vendor: {}",
                authentication.getName()
        );

        JwtAuthenticationDetails details =
                (JwtAuthenticationDetails) authentication.getDetails();

        User vendor = userRepository
                .findById(details.getUserId())
                .orElseThrow(() ->
                        new RuntimeException("Vendor not found")
                );

        List<ProductResponse> products =
                productService.getVendorProducts(vendor);

        return ResponseEntity.ok(
                new ApiResponse(
                        HttpStatus.OK.value(),
                        "Vendor products retrieved successfully",
                        products
                )
        );
    }
}