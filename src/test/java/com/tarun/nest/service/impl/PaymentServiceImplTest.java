package com.tarun.nest.service.impl;

import com.razorpay.OrderClient;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.tarun.nest.dto.CustomerAddressRequest;
import com.tarun.nest.dto.CreatePaymentRequest;
import com.tarun.nest.entity.CustomerAddress;
import com.tarun.nest.entity.PaymentOrder;
import com.tarun.nest.entity.Product;
import com.tarun.nest.entity.User;
import com.tarun.nest.repository.CustomerAddressRepository;
import com.tarun.nest.repository.PaymentOrderRepository;
import com.tarun.nest.repository.ProductRepository;
import com.tarun.nest.repository.UserRepository;
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

    @Mock
    private UserRepository userRepository;

    @Mock
    private CustomerAddressRepository addressRepository;

    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setUp() {
        razorpayClient.orders = orderClient;
        paymentService = new PaymentServiceImpl(
                razorpayClient,
                paymentRepository,
                productRepository,
                userRepository,
                addressRepository,
                "rzp_test_key",
                "test_secret"
        );
    }

    @Test
    void paymentOrderRequiresSavedCustomerAddress() throws Exception {
        User customer = new User();
        customer.setId(10L);
        when(userRepository.findByEmailForUpdate("payment-test@example.test"))
                .thenReturn(Optional.of(customer));
        when(addressRepository.findByIdAndCustomerId(3L, 10L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> paymentService.createOrder(
                        new CreatePaymentRequest(7L, 1, 3L, null),
                        "payment-test@example.test"
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(productRepository, never()).findById(any());
        verify(paymentRepository, never()).saveAndFlush(any(PaymentOrder.class));
    }

    @Test
    void rejectedGatewayOrderIsReturnedAsBadGateway() throws Exception {
        User customer = new User();
        customer.setId(10L);
        when(userRepository.findByEmailForUpdate("payment-test@example.test"))
                .thenReturn(Optional.of(customer));
        when(addressRepository.findByIdAndCustomerId(3L, 10L))
                .thenReturn(Optional.of(new CustomerAddress()));

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
                        new CreatePaymentRequest(7L, 1, 3L, null),
                        "payment-test@example.test"
                )
        );

        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode());
        verify(paymentRepository).saveAndFlush(any(PaymentOrder.class));
        verify(paymentRepository, never()).save(any(PaymentOrder.class));
    }

    @Test
    void orderPayloadAddressIsSavedForAuthenticatedCustomer() throws Exception {
        User customer = new User();
        customer.setId(10L);
        when(userRepository.findByEmailForUpdate("payment-test@example.test"))
                .thenReturn(Optional.of(customer));
        when(addressRepository.countByCustomerId(10L)).thenReturn(0L);
        when(addressRepository.saveAndFlush(any(CustomerAddress.class)))
                .thenAnswer(invocation -> {
                    CustomerAddress address = invocation.getArgument(0);
                    address.setCustomer(customer);
                    return address;
                });

        Product product = new Product();
        product.setId(7L);
        product.setName("Apple");
        product.setPrice(new BigDecimal("1.00"));
        when(productRepository.findById(7L)).thenReturn(Optional.of(product));
        when(paymentRepository.saveAndFlush(any(PaymentOrder.class)))
                .thenAnswer(invocation -> {
                    return invocation.getArgument(0);
                });
        Order razorpayOrder = org.mockito.Mockito.mock(Order.class);
        when(orderClient.create(any(JSONObject.class))).thenReturn(razorpayOrder);
        when(razorpayOrder.get("id")).thenReturn("razorpay_order_1");
        when(paymentRepository.save(any(PaymentOrder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        paymentService.createOrder(
                new CreatePaymentRequest(
                        7L,
                        1,
                        null,
                        new CustomerAddressRequest(
                                " Work ",
                                " 8 Main Street ",
                                " Pune ",
                                " Maharashtra ",
                                " 411001 ",
                                " India "
                        )
                ),
                "payment-test@example.test"
        );

        org.mockito.ArgumentCaptor<CustomerAddress> addressCaptor =
                org.mockito.ArgumentCaptor.forClass(CustomerAddress.class);
        verify(addressRepository).saveAndFlush(addressCaptor.capture());
        CustomerAddress savedAddress = addressCaptor.getValue();
        assertEquals(customer, savedAddress.getCustomer());
        assertEquals("Work", savedAddress.getLabel());
        assertEquals("8 Main Street", savedAddress.getAddress());

        org.mockito.ArgumentCaptor<PaymentOrder> orderCaptor =
                org.mockito.ArgumentCaptor.forClass(PaymentOrder.class);
        verify(paymentRepository).saveAndFlush(orderCaptor.capture());
        assertEquals(savedAddress, orderCaptor.getValue().getDeliveryAddress());
    }
}
