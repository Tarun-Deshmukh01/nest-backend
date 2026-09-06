package com.tarun.nest.service;

import com.tarun.nest.dto.AddToCartRequest;
import com.tarun.nest.entity.Cart;
import com.tarun.nest.entity.CartItem;
import com.tarun.nest.entity.Product;
import com.tarun.nest.entity.ProductStatus;
import com.tarun.nest.entity.User;
import com.tarun.nest.repository.CartItemRepository;
import com.tarun.nest.repository.CartRepository;
import com.tarun.nest.repository.ProductRepository;
import com.tarun.nest.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public CartItem addToCart(Long userId, AddToCartRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (user.getRole() != com.tarun.nest.entity.Role.CUSTOMER) {
            throw new RuntimeException(
                    "Only customers can add products to cart"
            );
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() ->
                        new RuntimeException("Product not found"));

        if (product.getStatus() != ProductStatus.PUBLISHED) {
            throw new RuntimeException(
                    "Product is not available"
            );
        }

        if (request.getQuantity() > product.getStock()) {
            throw new RuntimeException(
                    "Requested quantity exceeds available stock"
            );
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createCart(user));

        CartItem cartItem = cartItemRepository
                .findByCartIdAndProductId(
                        cart.getId(),
                        product.getId()
                )
                .orElse(null);

        if (cartItem != null) {

            int newQuantity =
                    cartItem.getQuantity() + request.getQuantity();

            if (newQuantity > product.getStock()) {
                throw new RuntimeException(
                        "Requested quantity exceeds available stock"
                );
            }

            cartItem.setQuantity(newQuantity);

        } else {

            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(request.getQuantity());
        }

        return cartItemRepository.save(cartItem);
    }

    private Cart createCart(User user) {

        Cart cart = new Cart();
        cart.setUser(user);

        return cartRepository.save(cart);
    }
}