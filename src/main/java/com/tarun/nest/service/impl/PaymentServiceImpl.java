package com.tarun.nest.service.impl;

import com.tarun.nest.dto.PaymentRequest;
import com.tarun.nest.dto.PaymentResponse;
import com.tarun.nest.entity.Cart;
import com.tarun.nest.entity.Order;
import com.tarun.nest.entity.Payment;
import com.tarun.nest.enums.OrderStatus;
import com.tarun.nest.enums.PaymentStatus;
import com.tarun.nest.repository.CartItemRepository;
import com.tarun.nest.repository.CartRepository;
import com.tarun.nest.repository.OrderRepository;
import com.tarun.nest.repository.PaymentRepository;
import com.tarun.nest.service.PaymentService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    @Override
    @Transactional
    public PaymentResponse processPayment(
            PaymentRequest request,
            Long userId
    ) {

        // 1. Find the order
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() ->
                        new RuntimeException("Order not found"));

        // 2. Make sure the order belongs to the logged-in customer
        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException(
                    "You are not authorized to pay for this order"
            );
        }

        // 3. Make sure order is waiting for payment
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new RuntimeException(
                    "Order is not available for payment"
            );
        }

        // 4. Get amount FROM ORDER
        var amount = order.getTotalAmount();

        // 5. Generate dummy transaction ID
        String transactionId =
                "DUMMY-" +
                UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase();

        // 6. Create payment
        Payment payment = Payment.builder()
                .userId(userId)
                .orderId(order.getId())
                .amount(amount)
                .paymentMethod(request.getPaymentMethod())
                .transactionId(transactionId)
                .status(PaymentStatus.SUCCESS)
                .createdAt(LocalDateTime.now())
                .build();

        Payment savedPayment =
                paymentRepository.save(payment);

        // 7. Update order after successful payment
        order.setStatus(OrderStatus.CONFIRMED);
        order.setUpdatedAt(LocalDateTime.now());

        orderRepository.save(order);

        // 8. Payment is successful.
        //    Now remove the products from the customer's cart.
        Cart cart = cartRepository.findByUserId(userId)
                .orElse(null);

        if (cart != null &&
                cart.getItems() != null &&
                !cart.getItems().isEmpty()) {

            cartItemRepository.deleteAll(cart.getItems());

            // Keep the in-memory cart object in sync
            cart.getItems().clear();
        }

        // 9. Return payment response
        return PaymentResponse.builder()
                .paymentId(savedPayment.getId())
                .orderId(savedPayment.getOrderId())
                .amount(savedPayment.getAmount())
                .status(savedPayment.getStatus())
                .paymentMethod(savedPayment.getPaymentMethod())
                .transactionId(savedPayment.getTransactionId())
                .message("Dummy payment successful")
                .build();
    }
}
