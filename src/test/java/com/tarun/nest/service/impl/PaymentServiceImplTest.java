package com.tarun.nest.service.impl;

import com.razorpay.OrderClient;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.tarun.nest.dto.CreatePaymentRequest;
import com.tarun.nest.entity.PaymentOrder;
import com.tarun.nest.entity.Product;
import com.tarun.nest.repository.PaymentOrderRepository;
import com.tarun.nest.repository.ProductRepository;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private RazorpayClient razorpayClient;

    @Mock
    private OrderClient orderClient;

    @Mock
    private PaymentOrderRepository paymentRepository;

    @Mock
    private ProductRepository productRepository;

    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setUp() {
        razorpayClient.orders = orderClient;
        paymentService = new PaymentServiceImpl(
                razorpayClient,
                paymentRepository,
                productRepository,
                "rzp_test_key",
                "test_secret"
        );
    }

    @Test
    void rejectedGatewayOrderIsReturnedAsBadGateway() throws Exception {
        Product product = new Product();
        product.setId(7L);
        product.setName("Apple");
        product.setPrice(new BigDecimal("1.00"));

        when(productRepository.findById(7L)).thenReturn(Optional.of(product));
        when(paymentRepository.saveAndFlush(any(PaymentOrder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(orderClient.create(any(JSONObject.class)))
                .thenThrow(new RazorpayException("Authentication failed"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> paymentService.createOrder(
                        new CreatePaymentRequest(7L, 1),
                        "payment-test@example.test"
                )
        );

        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode());
        verify(paymentRepository).saveAndFlush(any(PaymentOrder.class));
        verify(paymentRepository, never()).save(any(PaymentOrder.class));
    }
}
