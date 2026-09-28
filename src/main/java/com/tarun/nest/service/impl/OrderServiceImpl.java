package com.tarun.nest.service.impl;

import com.tarun.nest.dto.CreateOrderRequest;
import com.tarun.nest.dto.OrderItemResponse;
import com.tarun.nest.dto.OrderResponse;
import com.tarun.nest.entity.Cart;
import com.tarun.nest.entity.CartItem;
import com.tarun.nest.entity.Order;
import com.tarun.nest.entity.OrderItem;
import com.tarun.nest.entity.Product;
import com.tarun.nest.entity.User;
import com.tarun.nest.enums.OrderStatus;
import com.tarun.nest.repository.CartRepository;
import com.tarun.nest.repository.OrderRepository;
import com.tarun.nest.repository.UserRepository;
import com.tarun.nest.service.OrderService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;

    @Override
    @Transactional
    public OrderResponse createOrder(
            CreateOrderRequest request,
            Long userId
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new RuntimeException("Cart not found"));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING_PAYMENT)
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .country(request.getCountry())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cart.getItems()) {

            Product product = cartItem.getProduct();

            BigDecimal price = product.getPrice();

            Integer quantity = cartItem.getQuantity();

            BigDecimal subtotal =
                    price.multiply(
                            BigDecimal.valueOf(quantity)
                    );

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(quantity)
                    .price(price)
                    .build();

            orderItems.add(orderItem);

            totalAmount = totalAmount.add(subtotal);
        }

        order.setTotalAmount(totalAmount);
        order.setItems(orderItems);

        Order savedOrder = orderRepository.save(order);

        List<OrderItemResponse> itemResponses =
                new ArrayList<>();

        for (OrderItem item : savedOrder.getItems()) {

            BigDecimal subtotal =
                    item.getPrice().multiply(
                            BigDecimal.valueOf(item.getQuantity())
                    );

            itemResponses.add(
                    OrderItemResponse.builder()
                            .productId(item.getProduct().getId())
                            .productName(item.getProduct().getName())
                            .quantity(item.getQuantity())
                            .price(item.getPrice())
                            .subtotal(subtotal)
                            .build()
            );
        }

        return OrderResponse.builder()
                .orderId(savedOrder.getId())
                .totalAmount(savedOrder.getTotalAmount())
                .status(savedOrder.getStatus())
                .address(savedOrder.getAddress())
                .city(savedOrder.getCity())
                .state(savedOrder.getState())
                .pincode(savedOrder.getPincode())
                .country(savedOrder.getCountry())
                .createdAt(savedOrder.getCreatedAt())
                .items(itemResponses)
                .build();
    }
}