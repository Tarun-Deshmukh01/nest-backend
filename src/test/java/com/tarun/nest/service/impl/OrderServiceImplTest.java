package com.tarun.nest.service.impl;

import com.tarun.nest.dto.CreateOrderRequest;
import com.tarun.nest.entity.*;
import com.tarun.nest.enums.OrderStatus;
import com.tarun.nest.repository.*;
import com.tarun.nest.service.RealtimeNotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private UserRepository userRepository;
    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductRepository productRepository;
    @Mock private CustomerAddressRepository addressRepository;
    @Mock private VendorOrderFulfillmentRepository fulfillmentRepository;
    @Mock private RealtimeNotificationService notificationService;

    @Test
    void createsPendingOrderFromServerCartAndReservesStock() {
        User customer = new User();
        customer.setId(4L);
        customer.setName("Customer");
        customer.setMobileNumber("9876543210");
        User vendor = new User();
        vendor.setId(8L);
        vendor.setName("Vendor");
        Product product = new Product();
        product.setId(12L);
        product.setName("Apples");
        product.setImageUrl("https://example.test/apple.png");
        product.setPrice(new BigDecimal("25.00"));
        product.setStock(5);
        product.setVendor(vendor);
        product.setStatus(ProductStatus.PUBLISHED);
        Cart cart = new Cart();
        cart.setId(20L);
        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        CustomerAddress address = new CustomerAddress();
        address.setCustomer(customer);
        address.setAddress("1 Main Street");
        address.setCity("Pune");
        address.setState("Maharashtra");
        address.setPincode("411001");
        address.setCountry("India");

        when(userRepository.findByIdForUpdate(4L)).thenReturn(Optional.of(customer));
        when(orderRepository.findByUserIdAndIdempotencyKey(4L, "checkout-123"))
                .thenReturn(Optional.empty());
        when(addressRepository.findByIdAndCustomerId(30L, 4L))
                .thenReturn(Optional.of(address));
        when(cartRepository.findByUserId(4L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(20L)).thenReturn(List.of(cartItem));
        when(productRepository.findByIdForUpdate(12L)).thenReturn(Optional.of(product));
        when(orderRepository.saveAndFlush(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrderServiceImpl service = new OrderServiceImpl(
                orderRepository,
                userRepository,
                cartRepository,
                cartItemRepository,
                productRepository,
                addressRepository,
                fulfillmentRepository,
                notificationService
        );
        CreateOrderRequest request = new CreateOrderRequest();
        request.setAddressId(30L);

        var result = service.createOrder(request, 4L, "checkout-123");

        assertEquals(OrderStatus.PENDING_PAYMENT, result.getStatus());
        assertEquals(new BigDecimal("50.00"), result.getTotalAmount());
        assertEquals("1 Main Street", result.getAddress());
        assertEquals("Apples", result.getItems().get(0).getProductName());
        assertEquals(3, product.getStock());
        verify(fulfillmentRepository).save(any(VendorOrderFulfillment.class));
        verify(notificationService, never()).sendNotificationToUsers(
                any(), any(), any(), any()
        );
    }

    @Test
    void rejectsOrderWhenCartQuantityExceedsLockedStock() {
        User customer = new User();
        customer.setId(4L);
        Product product = new Product();
        product.setId(12L);
        product.setName("Apples");
        product.setStock(1);
        product.setStatus(ProductStatus.PUBLISHED);
        Cart cart = new Cart();
        cart.setId(20L);
        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        CustomerAddress address = new CustomerAddress();

        when(userRepository.findByIdForUpdate(4L)).thenReturn(Optional.of(customer));
        when(orderRepository.findByUserIdAndIdempotencyKey(4L, "checkout-123"))
                .thenReturn(Optional.empty());
        when(addressRepository.findByIdAndCustomerId(30L, 4L))
                .thenReturn(Optional.of(address));
        when(cartRepository.findByUserId(4L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(20L)).thenReturn(List.of(cartItem));
        when(productRepository.findByIdForUpdate(12L)).thenReturn(Optional.of(product));

        OrderServiceImpl service = new OrderServiceImpl(
                orderRepository,
                userRepository,
                cartRepository,
                cartItemRepository,
                productRepository,
                addressRepository,
                fulfillmentRepository,
                notificationService
        );
        CreateOrderRequest request = new CreateOrderRequest();
        request.setAddressId(30L);

        assertThrows(
                ResponseStatusException.class,
                () -> service.createOrder(request, 4L, "checkout-123")
        );
        assertEquals(1, product.getStock());
        verify(orderRepository, never()).saveAndFlush(any(Order.class));
    }
}
