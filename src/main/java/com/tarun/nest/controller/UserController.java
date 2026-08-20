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
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse> getProfile(Authentication authentication) {
        log.info("Fetching profile for user: {}", authentication.getName());
        JwtAuthenticationDetails details = (JwtAuthenticationDetails) authentication.getDetails();
        
        return ResponseEntity.ok(new ApiResponse(
                HttpStatus.OK.value(),
                "Profile retrieved successfully",
                details
        ));
    }

    @GetMapping("/hello")
    public ResponseEntity<ApiResponse> helloUser(Authentication authentication) {
        log.info("Hello endpoint accessed by: {}", authentication.getName());
        
        return ResponseEntity.ok(new ApiResponse(
                HttpStatus.OK.value(),
                "Hello " + authentication.getName() + "! You are a USER.",
                null
        ));
    }

    @PostMapping("/cart/add")
    public ResponseEntity<ApiResponse> addToCart(
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") Integer quantity,
            Authentication authentication) {
        log.info("User {} adding product {} to cart", authentication.getName(), productId);
        
        return ResponseEntity.ok(new ApiResponse(
                HttpStatus.OK.value(),
                "Product added to cart successfully",
                null
        ));
    }

    @GetMapping("/cart")
    public ResponseEntity<ApiResponse> getCart(Authentication authentication) {
        log.info("Fetching cart for user: {}", authentication.getName());
        
        return ResponseEntity.ok(new ApiResponse(
                HttpStatus.OK.value(),
                "Cart retrieved successfully",
                null
        ));
    }

    @DeleteMapping("/cart/remove/{productId}")
    public ResponseEntity<ApiResponse> removeFromCart(
            @PathVariable Long productId,
            Authentication authentication) {
        log.info("User {} removing product {} from cart", authentication.getName(), productId);
        
        return ResponseEntity.ok(new ApiResponse(
                HttpStatus.OK.value(),
                "Product removed from cart successfully",
                null
        ));
    }

    @PostMapping("/orders/place")
    public ResponseEntity<ApiResponse> placeOrder(Authentication authentication) {
        log.info("User {} placing order", authentication.getName());
        
        return ResponseEntity.ok(new ApiResponse(
                HttpStatus.CREATED.value(),
                "Order placed successfully",
                null
        ));
    }

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse> getOrders(Authentication authentication) {
        log.info("Fetching orders for user: {}", authentication.getName());
        JwtAuthenticationDetails details = (JwtAuthenticationDetails) authentication.getDetails();
        
        return ResponseEntity.ok(new ApiResponse(
                HttpStatus.OK.value(),
                "Orders retrieved successfully",
                null
        ));
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<ApiResponse> getOrderDetails(
            @PathVariable Long orderId,
            Authentication authentication) {
        log.info("Fetching order {} for user: {}", orderId, authentication.getName());
        JwtAuthenticationDetails details = (JwtAuthenticationDetails) authentication.getDetails();
        
        return ResponseEntity.ok(new ApiResponse(
                HttpStatus.OK.value(),
                "Order details retrieved successfully",
                null
        ));
    }

    @PostMapping("/wishlist/add")
    public ResponseEntity<ApiResponse> addToWishlist(
            @RequestParam Long productId,
            Authentication authentication) {
        log.info("User {} adding product {} to wishlist", authentication.getName(), productId);
        
        return ResponseEntity.ok(new ApiResponse(
                HttpStatus.OK.value(),
                "Product added to wishlist successfully",
                null
        ));
    }

    @GetMapping("/wishlist")
    public ResponseEntity<ApiResponse> getWishlist(Authentication authentication) {
        log.info("Fetching wishlist for user: {}", authentication.getName());
        
        return ResponseEntity.ok(new ApiResponse(
                HttpStatus.OK.value(),
                "Wishlist retrieved successfully",
                null
        ));
    }

    @DeleteMapping("/wishlist/remove/{productId}")
    public ResponseEntity<ApiResponse> removeFromWishlist(
            @PathVariable Long productId,
            Authentication authentication) {
        log.info("User {} removing product {} from wishlist", authentication.getName(), productId);
        
        return ResponseEntity.ok(new ApiResponse(
                HttpStatus.OK.value(),
                "Product removed from wishlist successfully",
                null
        ));
    }
}
