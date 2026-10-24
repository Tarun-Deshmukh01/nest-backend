package com.tarun.nest.service.impl;

import com.razorpay.OrderClient;
import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.PaymentClient;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import com.tarun.nest.dto.CustomerAddressRequest;
import com.tarun.nest.dto.CreatePaymentRequest;
import com.tarun.nest.dto.CreatePaymentResponse;
import com.tarun.nest.dto.VerifyPaymentRequest;
import com.tarun.nest.entity.CustomerAddress;
import com.tarun.nest.entity.PaymentOrder;
import com.tarun.nest.entity.Product;
import com.tarun.nest.entity.User;
import com.tarun.nest.repository.CustomerAddressRepository;
import com.tarun.nest.repository.PaymentOrderRepository;
import com.tarun.nest.repository.ProductRepository;
import com.tarun.nest.repository.UserRepository;
import com.tarun.nest.repository.OrderRepository;
import com.tarun.nest.repository.VendorOrderFulfillmentRepository;
import com.tarun.nest.repository.CartItemRepository;
import com.tarun.nest.repository.CartRepository;
import com.tarun.nest.service.RealtimeNotificationService;
import com.tarun.nest.service.OrderPaymentPreparationService;
import com.tarun.nest.service.OrderService;
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
    private PaymentClient paymentClient;

    @Mock
    private PaymentOrderRepository paymentRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CustomerAddressRepository addressRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private VendorOrderFulfillmentRepository fulfillmentRepository;

    @Mock
    private RealtimeNotificationService notificationService;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private OrderPaymentPreparationService orderPaymentPreparationService;

    @Mock
    private OrderService orderService;

    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setUp() {
        razorpayClient.orders = orderClient;
        razorpayClient.payments = paymentClient;
        paymentService = new PaymentServiceImpl(
                razorpayClient,
                paymentRepository,
                productRepository,
                userRepository,
                addressRepository,
                orderRepository,
                fulfillmentRepository,
                notificationService,
                cartRepository,
                cartItemRepository,
                orderPaymentPreparationService,
                orderService,
                "rzp_test_key",
                "test_secret",
                "webhook_test_secret"
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
                                null,
                                null,
                                " 8 Main Street ",
                                null,
                                null,
                                " Pune ",
                                " Maharashtra ",
                                " 411001 ",
                                " India ",
                                false
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

    @Test
    void reusesExistingProviderOrderOnPaymentRetry() throws Exception {
        when(orderPaymentPreparationService.prepare(
                22L, "payment-test@example.test"
        )).thenReturn(new OrderPaymentPreparationService.PreparedOrderPayment(
                33L, "razorpay_order_existing", 2500L, "INR",
                "NEST-ORDER", false
        ));

        CreatePaymentResponse response = paymentService.createPaymentForOrder(
                22L, "payment-test@example.test"
        );

        assertEquals("razorpay_order_existing", response.razorpayOrderId());
        assertEquals(2500L, response.amount());
        verify(orderClient, never()).create(any(JSONObject.class));
    }

    @Test
    void createsProviderOrderAfterPersistingPaymentAttempt() throws Exception {
        when(orderPaymentPreparationService.prepare(
                22L, "payment-test@example.test"
        )).thenReturn(new OrderPaymentPreparationService.PreparedOrderPayment(
                33L, null, 2500L, "INR", "NEST-ORDER", true
        ));
        Order razorpayOrder = org.mockito.Mockito.mock(Order.class);
        when(orderClient.create(any(JSONObject.class))).thenReturn(razorpayOrder);
        when(razorpayOrder.get("id")).thenReturn("razorpay_order_new");

        CreatePaymentResponse response = paymentService.createPaymentForOrder(
                22L, "payment-test@example.test"
        );

        assertEquals("razorpay_order_new", response.razorpayOrderId());
        verify(orderPaymentPreparationService)
                .markCreated(33L, "razorpay_order_new");
    }

    @Test
    void verifiesCapturedPaymentAndTreatsDuplicateCallbackIdempotently()
            throws Exception {
        PaymentOrder internalOrder = new PaymentOrder();
        internalOrder.setCustomerEmail("payment-test@example.test");
        internalOrder.setAmountPaise(100L);
        internalOrder.setRazorpayOrderId("razorpay_order_1");
        Payment providerPayment = org.mockito.Mockito.mock(Payment.class);
        when(paymentRepository.findByIdAndCustomerEmailForUpdate(
                44L, "payment-test@example.test"
        )).thenReturn(Optional.of(internalOrder));
        when(paymentClient.fetch("razorpay_payment_1"))
                .thenReturn(providerPayment);
        when(providerPayment.get("order_id")).thenReturn("razorpay_order_1");
        when(providerPayment.get("amount")).thenReturn(100L);
        when(providerPayment.get("currency")).thenReturn("INR");
        when(providerPayment.get("status")).thenReturn("captured");
        String signature = Utils.getHash(
                "razorpay_order_1|razorpay_payment_1", "test_secret"
        );
        VerifyPaymentRequest request = new VerifyPaymentRequest(
                44L,
                "razorpay_order_1",
                "razorpay_payment_1",
                signature
        );

        var firstResponse = paymentService.verifyPayment(
                request, "payment-test@example.test"
        );
        var duplicateResponse = paymentService.verifyPayment(
                request, "payment-test@example.test"
        );

        assertEquals("PAID", firstResponse.status());
        assertEquals("PAID", duplicateResponse.status());
        assertEquals(PaymentOrder.PaymentStatus.PAID, internalOrder.getStatus());
        verify(paymentClient).fetch("razorpay_payment_1");
    }
}
