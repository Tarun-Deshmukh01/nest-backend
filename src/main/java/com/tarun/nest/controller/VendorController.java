package com.tarun.nest.controller;

import com.tarun.nest.dto.ApiResponse;
import com.tarun.nest.security.JwtAuthenticationDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vendor")
@RequiredArgsConstructor
@Slf4j
public class VendorController {

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse> getDashboard(Authentication authentication) {
        log.info("Fetching dashboard for vendor: {}", authentication.getName());
        JwtAuthenticationDetails details = (JwtAuthenticationDetails) authentication.getDetails();
        
        return ResponseEntity.ok(new ApiResponse(
                HttpStatus.OK.value(),
                "Vendor dashboard retrieved successfully",
                null
        ));
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse> getProfile(Authentication authentication) {
        log.info("Fetching vendor profile for: {}", authentication.getName());
        JwtAuthenticationDetails details = (JwtAuthenticationDetails) authentication.getDetails();
        
        return ResponseEntity.ok(new ApiResponse(
                HttpStatus.OK.value(),
                "Vendor profile retrieved successfully",
                details
        ));
    }

    @PostMapping("/products/add")
    public ResponseEntity<ApiResponse> addProduct(
            @RequestBody String productData,
            Authentication authentication) {
        log.info("Vendor {} adding new product", authentication.getName());
        JwtAuthenticationDetails details = (JwtAuthenticationDetails) authentication.getDetails();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(
                HttpStatus.CREATED.value(),
                "Product added successfully",
                null
        ));
    }

    @GetMapping("/products")
    public ResponseEntity<ApiResponse> getProducts(Authentication authentication) {
        log.info("Fetching products for vendor: {}", authentication.getName());
        JwtAuthenticationDetails details = (JwtAuthenticationDetails) authentication.getDetails();
        
        return ResponseEntity.ok(new ApiResponse(
                HttpStatus.OK.value(),
                "Vendor products retrieved successfully",
                null
        ));
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<ApiResponse> getProductDetails(
            @PathVariable Long productId,
            Authentication authentication) {
        log.info("Fetching product {} details for vendor: {}", productId, authentication.getName());
        JwtAuthenticationDetails details = (JwtAuthenticationDetails) authentication.getDetails();
        
        return ResponseEntity.ok(new ApiResponse(
                HttpStatus.OK.value(),
                "Product details retrieved successfully",
                null
        ));
    }

    @PutMapping("/products/{productId}/update")
    public ResponseEntity<ApiResponse> updateProduct(
            @PathVariable Long productId,
            @RequestBody String productData,
            Authentication authentication) {
        log.info("Vendor {} updating product {}", authentication.getName(), productId);
        JwtAuthenticationDetails details = (JwtAuthenticationDetails) authentication.getDetails();
        
        return ResponseEntity.ok(new ApiResponse(
                HttpStatus.OK.value(),
                "Product updated successfully",
                null
        ));
    }

    @DeleteMapping("/products/{productId}/delete")
    public ResponseEntity<ApiResponse> deleteProduct(
            @PathVariable Long productId,
            Authentication authentication) {
        log.info("Vendor {} deleting product {}", authentication.getName(), productId);
        JwtAuthenticationDetails details = (JwtAuthenticationDetails) authentication.getDetails();
        
        return ResponseEntity.ok(new ApiResponse(
                HttpStatus.OK.value(),
                "Product deleted successfully",
                null
        ));
    }

    @GetMapping("/inventory")
    public ResponseEntity<ApiResponse> getInventory(Authentication authentication) {
        log.info("Fetching inventory for vendor: {}", authentication.getName());
        JwtAuthenticationDetails details = (JwtAuthenticationDetails) authentication.getDetails();
        
        return ResponseEntity.ok(new ApiResponse(
                HttpStatus.OK.value(),
                "Inventory retrieved successfully",
                null
        ));
    }

    @PutMapping("/inventory/{productId}/update")
    public ResponseEntity<ApiResponse> updateInventory(
            @PathVariable Long productId,
            @RequestParam Integer quantity,
            Authentication authentication) {
        log.info("Vendor {} updating inventory for product {}", authentication.getName(), productId);
        JwtAuthenticationDetails details = (JwtAuthenticationDetails) authentication.getDetails();
        
        return ResponseEntity.ok(new ApiResponse(
                HttpStatus.OK.value(),
                "Inventory updated successfully",
                null
        ));
    }

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse> getSellerOrders(Authentication authentication) {
        log.info("Fetching seller orders for vendor: {}", authentication.getName());
        JwtAuthenticationDetails details = (JwtAuthenticationDetails) authentication.getDetails();
        
        return ResponseEntity.ok(new ApiResponse(
                HttpStatus.OK.value(),
                "Seller orders retrieved successfully",
                null
        ));
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<ApiResponse> getSellerOrderDetails(
            @PathVariable Long orderId,
            Authentication authentication) {
        log.info("Fetching order {} details for vendor: {}", orderId, authentication.getName());
        JwtAuthenticationDetails details = (JwtAuthenticationDetails) authentication.getDetails();
        
        return ResponseEntity.ok(new ApiResponse(
                HttpStatus.OK.value(),
                "Order details retrieved successfully",
                null
        ));
    }

    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<ApiResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String status,
            Authentication authentication) {
        log.info("Vendor {} updating order {} status to: {}", authentication.getName(), orderId, status);
        JwtAuthenticationDetails details = (JwtAuthenticationDetails) authentication.getDetails();
        
        return ResponseEntity.ok(new ApiResponse(
                HttpStatus.OK.value(),
                "Order status updated successfully",
                null
        ));
    }

    @GetMapping("/earnings")
    public ResponseEntity<ApiResponse> getEarnings(Authentication authentication) {
        log.info("Fetching earnings for vendor: {}", authentication.getName());
        JwtAuthenticationDetails details = (JwtAuthenticationDetails) authentication.getDetails();
        
        return ResponseEntity.ok(new ApiResponse(
                HttpStatus.OK.value(),
                "Earnings retrieved successfully",
                null
        ));
    }

    @GetMapping("/ratings")
    public ResponseEntity<ApiResponse> getRatings(Authentication authentication) {
        log.info("Fetching ratings for vendor: {}", authentication.getName());
        JwtAuthenticationDetails details = (JwtAuthenticationDetails) authentication.getDetails();
        
        return ResponseEntity.ok(new ApiResponse(
                HttpStatus.OK.value(),
                "Ratings retrieved successfully",
                null
        ));
    }
}
