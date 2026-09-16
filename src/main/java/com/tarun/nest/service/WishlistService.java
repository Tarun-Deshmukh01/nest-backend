package com.tarun.nest.service;

import com.tarun.nest.dto.WishlistResponse;

public interface WishlistService {

    WishlistResponse getWishlist(Long userId);

    WishlistResponse addToWishlist(Long userId, Long productId);

    WishlistResponse removeFromWishlist(Long userId, Long productId);

    boolean isInWishlist(Long userId, Long productId);
}