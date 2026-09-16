package com.tarun.nest.service;

import com.tarun.nest.dto.WishlistItemResponse;
import com.tarun.nest.dto.WishlistResponse;
import com.tarun.nest.entity.Product;
import com.tarun.nest.entity.User;
import com.tarun.nest.entity.Wishlist;
import com.tarun.nest.repository.ProductRepository;
import com.tarun.nest.repository.UserRepository;
import com.tarun.nest.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    public WishlistResponse getWishlist(Long userId) {

        User user = getUser(userId);

        List<WishlistItemResponse> items = wishlistRepository.findByUser(user)
                .stream()
                .map(this::mapToResponse)
                .toList();

        return WishlistResponse.builder()
                .items(items)
                .totalItems(items.size())
                .build();
    }

    @Override
    @Transactional
    public WishlistResponse addToWishlist(Long userId, Long productId) {

        User user = getUser(userId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new RuntimeException("Product not found"));

        if (!wishlistRepository.existsByUserAndProduct(user, product)) {

            Wishlist wishlist = Wishlist.builder()
                    .user(user)
                    .product(product)
                    .createdAt(LocalDateTime.now())
                    .build();

            wishlistRepository.save(wishlist);
        }

        return getWishlist(userId);
    }

    @Override
    @Transactional
    public WishlistResponse removeFromWishlist(Long userId, Long productId) {

        User user = getUser(userId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new RuntimeException("Product not found"));

        wishlistRepository.deleteByUserAndProduct(user, product);

        return getWishlist(userId);
    }

    @Override
    public boolean isInWishlist(Long userId, Long productId) {

        User user = getUser(userId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new RuntimeException("Product not found"));

        return wishlistRepository.existsByUserAndProduct(user, product);
    }

    private User getUser(Long userId) {

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }

    private WishlistItemResponse mapToResponse(Wishlist wishlist) {

        Product product = wishlist.getProduct();

        return WishlistItemResponse.builder()
                .productId(product.getId())
                .name(product.getName())
                .imageUrl(product.getImageUrl())
                .price(product.getPrice())
                .stock(product.getStock())
                .build();
    }
}