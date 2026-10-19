package com.tarun.nest.service;

import com.tarun.nest.entity.Order;
import com.tarun.nest.entity.PaymentOrder;
import com.tarun.nest.enums.OrderPaymentStatus;
import com.tarun.nest.enums.OrderStatus;
import com.tarun.nest.repository.OrderRepository;
import com.tarun.nest.repository.PaymentOrderRepository;
import com.tarun.nest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class OrderPaymentPreparationService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final PaymentOrderRepository paymentOrderRepository;

    @Transactional
    public PreparedOrderPayment prepare(Long orderId, String customerEmail) {
        var customer = userRepository.findByEmailForUpdate(customerEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Customer not found"
                ));
        Order order = orderRepository.findByIdAndUserIdForUpdate(
                        orderId, customer.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found"
                ));
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT
                || order.getPaymentStatus() == OrderPaymentStatus.PAID) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Order is not awaiting payment"
            );
        }

        PaymentOrder latestAttempt = paymentOrderRepository
                .findFirstByOrderIdOrderByIdDesc(orderId)
                .orElse(null);
        if (latestAttempt != null
                && latestAttempt.getStatus() == PaymentOrder.PaymentStatus.CREATING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Payment order creation is already in progress"
            );
        }
        if (latestAttempt != null
                && latestAttempt.getStatus() != PaymentOrder.PaymentStatus.FAILED
                && latestAttempt.getRazorpayOrderId() != null) {
            return new PreparedOrderPayment(
                    latestAttempt.getId(),
                    latestAttempt.getRazorpayOrderId(),
                    latestAttempt.getAmountPaise(),
                    latestAttempt.getCurrency(),
                    order.getOrderNumber(),
                    false
            );
        }

        long amountPaise = order.getTotalAmount()
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
        PaymentOrder attempt = new PaymentOrder();
        attempt.setCustomerEmail(customerEmail);
        attempt.setOrder(order);
        attempt.setAmountPaise(amountPaise);
        attempt.setStatus(PaymentOrder.PaymentStatus.CREATING);
        attempt = paymentOrderRepository.saveAndFlush(attempt);
        order.setPaymentStatus(OrderPaymentStatus.PENDING);
        orderRepository.save(order);
        return new PreparedOrderPayment(
                attempt.getId(),
                null,
                amountPaise,
                order.getCurrency(),
                order.getOrderNumber(),
                true
        );
    }

    @Transactional
    public void markCreated(Long paymentId, String razorpayOrderId) {
        PaymentOrder attempt = paymentOrderRepository.findById(paymentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Payment attempt not found"
                ));
        attempt.setRazorpayOrderId(razorpayOrderId);
        attempt.setStatus(PaymentOrder.PaymentStatus.CREATED);
        paymentOrderRepository.save(attempt);
    }

    @Transactional
    public void markFailed(Long paymentId) {
        PaymentOrder attempt = paymentOrderRepository.findById(paymentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Payment attempt not found"
                ));
        attempt.setStatus(PaymentOrder.PaymentStatus.FAILED);
        paymentOrderRepository.save(attempt);
    }

    public record PreparedOrderPayment(
            Long internalOrderId,
            String razorpayOrderId,
            Long amountPaise,
            String currency,
            String orderNumber,
            boolean createProviderOrder
    ) {
    }
}
