package com.tarun.nest.controller;

import com.tarun.nest.dto.WishlistResponse;
import com.tarun.nest.service.WishlistService;
import com.tarun.nest.util.AuthorizationUtil;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;
    private final AuthorizationUtil authorizationUtil;

    @PostMapping("/{productId}")
    public ResponseEntity<WishlistResponse> addToWishlist(
            @PathVariable Long productId,
            Authentication authentication
    ) {

        Long userId = authorizationUtil.getUserId(authentication);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(
                wishlistService.addToWishlist(userId, productId)
        );
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<WishlistResponse> removeFromWishlist(
            @PathVariable Long productId,
            Authentication authentication
    ) {

        Long userId = authorizationUtil.getUserId(authentication);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(
                wishlistService.removeFromWishlist(userId, productId)
        );
    }

    @GetMapping
    public ResponseEntity<WishlistResponse> getWishlist(
            Authentication authentication
    ) {

        Long userId = authorizationUtil.getUserId(authentication);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(
                wishlistService.getWishlist(userId)
        );
    }

    @GetMapping("/{productId}/exists")
    public ResponseEntity<Boolean> isInWishlist(
            @PathVariable Long productId,
            Authentication authentication
    ) {

        Long userId = authorizationUtil.getUserId(authentication);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(
                wishlistService.isInWishlist(userId, productId)
        );
    }
}