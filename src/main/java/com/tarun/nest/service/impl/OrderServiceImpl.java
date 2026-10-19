package com.tarun.nest.service.impl;

import com.tarun.nest.dto.CreateOrderRequest;
import com.tarun.nest.dto.OrderItemResponse;
import com.tarun.nest.dto.OrderResponse;
import com.tarun.nest.entity.*;
import com.tarun.nest.enums.OrderPaymentStatus;
import com.tarun.nest.enums.OrderStatus;
import com.tarun.nest.repository.*;
import com.tarun.nest.service.OrderService;
import com.tarun.nest.service.RealtimeNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final CustomerAddressRepository addressRepository;
    private final VendorOrderFulfillmentRepository fulfillmentRepository;
    private final RealtimeNotificationService notificationService;

    @Override
    @Transactional
    public OrderResponse createOrder(
            CreateOrderRequest request,
            Long userId,
            String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()
                || idempotencyKey.length() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "A valid Idempotency-Key header is required"
            );
        }
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Customer not found"
                ));
        Optional<Order> previousOrder = orderRepository
                .findByUserIdAndIdempotencyKey(userId, idempotencyKey);
        if (previousOrder.isPresent()) {
            return toResponse(previousOrder.get(), item -> true);
        }
        CustomerAddress address = addressRepository
                .findByIdAndCustomerId(request.getAddressId(), userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Delivery address not found"
                ));
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Cart is empty"
                ));
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        if (cartItems.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Cart is empty"
            );
        }

        Order order = Order.builder()
                .user(user)
                .orderNumber("NEST-" + UUID.randomUUID()
                        .toString().replace("-", "").substring(0, 16).toUpperCase(Locale.ROOT))
                .idempotencyKey(idempotencyKey)
                .status(OrderStatus.PENDING_PAYMENT)
                .paymentStatus(OrderPaymentStatus.CREATED)
                .recipientName(address.getRecipientName() == null
                        ? user.getName() : address.getRecipientName())
                .phoneNumber(address.getPhoneNumber() == null
                        ? user.getMobileNumber() : address.getPhoneNumber())
                .address(address.getAddress())
                .addressLine2(address.getAddressLine2())
                .landmark(address.getLandmark())
                .city(address.getCity())
                .state(address.getState())
                .pincode(address.getPincode())
                .country(address.getCountry())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        Map<Long, User> vendors = new LinkedHashMap<>();
        for (CartItem cartItem : cartItems) {
            Product product = productRepository.findByIdForUpdate(
                            cartItem.getProduct().getId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "A cart product no longer exists"
                    ));
            int quantity = cartItem.getQuantity();
            if (product.getStatus() != ProductStatus.PUBLISHED
                    || quantity < 1
                    || quantity > 100
                    || product.getStock() == null
                    || product.getStock() < quantity
                    || product.getPrice() == null
                    || product.getPrice().signum() <= 0) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Product is unavailable or no longer has enough stock: "
                                + product.getName()
                );
            }
            product.setStock(product.getStock() - quantity);
            BigDecimal lineTotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(quantity));
            subtotal = subtotal.add(lineTotal);

            User vendor = product.getVendor();
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(quantity)
                    .price(product.getPrice())
                    .productNameSnapshot(product.getName())
                    .productImageSnapshot(product.getImageUrl())
                    .vendorUserIdSnapshot(vendor.getId())
                    .vendorNameSnapshot(vendor.getName())
                    .lineTotal(lineTotal)
                    .build();
            orderItems.add(orderItem);
            vendors.put(vendor.getId(), vendor);
        }

        BigDecimal deliveryCharges = BigDecimal.ZERO;
        order.setSubtotal(subtotal);
        order.setDeliveryCharges(deliveryCharges);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setTotalAmount(subtotal.add(deliveryCharges));
        order.setItems(orderItems);
        Order savedOrder = orderRepository.saveAndFlush(order);

        vendors.values().forEach(vendor -> {
            VendorOrderFulfillment fulfillment = new VendorOrderFulfillment();
            fulfillment.setOrder(savedOrder);
            fulfillment.setVendor(vendor);
            fulfillment.setStatus(OrderStatus.PENDING_PAYMENT);
            fulfillmentRepository.save(fulfillment);
        });
        return toResponse(savedOrder, item -> true);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(order -> toResponse(order, item -> true))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getMyOrder(Long orderId, Long userId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found"
                ));
        return toResponse(order, item -> true);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getVendorOrders(Long vendorUserId) {
        return orderRepository.findByVendorUserId(vendorUserId).stream()
                .map(order -> {
                    OrderResponse response = toResponse(order,
                            item -> isVendorItem(item, vendorUserId));
                    fulfillmentRepository.findByOrderIdAndVendorId(
                                    order.getId(), vendorUserId)
                            .ifPresent(fulfillment ->
                                    response.setStatus(fulfillment.getStatus()));
                    return response;
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getVendorOrder(Long orderId, Long vendorUserId) {
        Order order = orderRepository.findByIdAndVendorUserId(
                        orderId, vendorUserId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found"
                ));
        OrderResponse response = toResponse(order,
                item -> isVendorItem(item, vendorUserId));
        fulfillmentRepository.findByOrderIdAndVendorId(orderId, vendorUserId)
                .ifPresent(fulfillment -> response.setStatus(fulfillment.getStatus()));
        return response;
    }

    @Override
    @Transactional
    public OrderResponse updateVendorOrderStatus(
            Long orderId,
            Long vendorUserId,
            OrderStatus status) {
        VendorOrderFulfillment fulfillment = fulfillmentRepository
                .findByOrderIdAndVendorId(orderId, vendorUserId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found"
                ));
        if (fulfillment.getOrder().getPaymentStatus() != OrderPaymentStatus.PAID) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Fulfillment can start only after confirmed payment"
            );
        }
        OrderStatus current = fulfillment.getStatus();
        boolean validTransition = switch (current) {
            case PENDING_PAYMENT -> status == OrderStatus.PROCESSING;
            case PROCESSING -> status == OrderStatus.SHIPPED;
            case SHIPPED -> status == OrderStatus.OUT_FOR_DELIVERY;
            case OUT_FOR_DELIVERY -> status == OrderStatus.DELIVERED;
            default -> false;
        };
        if (!validTransition) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Invalid fulfillment status transition"
            );
        }
        fulfillment.setStatus(status);
        fulfillmentRepository.save(fulfillment);
        Order order = fulfillment.getOrder();
        order.setStatus(aggregateStatus(order.getId()));
        orderRepository.save(order);
        notificationService.sendNotificationToUsers(
                List.of(order.getUser().getId()),
                "Order status updated",
                "Order " + order.getOrderNumber() + " is now " + status + ".",
                "/orders"
        );
        return getVendorOrder(orderId, vendorUserId);
    }

    private OrderStatus aggregateStatus(Long orderId) {
        List<OrderStatus> statuses = fulfillmentRepository.findByOrderId(orderId)
                .stream()
                .map(VendorOrderFulfillment::getStatus)
                .toList();
        if (statuses.isEmpty()) {
            return OrderStatus.CONFIRMED;
        }
        if (statuses.stream().allMatch(status -> status == OrderStatus.DELIVERED)) {
            return OrderStatus.DELIVERED;
        }
        if (statuses.stream().anyMatch(status -> status == OrderStatus.PENDING_PAYMENT)) {
            return OrderStatus.CONFIRMED;
        }
        if (statuses.stream().anyMatch(status -> status == OrderStatus.PROCESSING)) {
            return OrderStatus.PROCESSING;
        }
        if (statuses.stream().anyMatch(status -> status == OrderStatus.SHIPPED)) {
            return OrderStatus.SHIPPED;
        }
        return OrderStatus.OUT_FOR_DELIVERY;
    }

    private boolean isVendorItem(OrderItem item, Long vendorUserId) {
        Long itemVendorId = item.getVendorUserIdSnapshot() == null
                ? item.getProduct().getVendor().getId()
                : item.getVendorUserIdSnapshot();
        return Objects.equals(itemVendorId, vendorUserId);
    }

    private OrderResponse toResponse(Order order, Predicate<OrderItem> includeItem) {
        List<OrderItemResponse> items = order.getItems().stream()
                .filter(includeItem)
                .map(item -> OrderItemResponse.builder()
                        .productId(item.getProduct().getId())
                        .productName(item.getProductNameSnapshot() == null
                                ? item.getProduct().getName()
                                : item.getProductNameSnapshot())
                        .productImage(item.getProductImageSnapshot() == null
                                ? item.getProduct().getImageUrl()
                                : item.getProductImageSnapshot())
                        .vendorId(item.getVendorUserIdSnapshot() == null
                                ? item.getProduct().getVendor().getId()
                                : item.getVendorUserIdSnapshot())
                        .vendorName(item.getVendorNameSnapshot() == null
                                ? item.getProduct().getVendor().getName()
                                : item.getVendorNameSnapshot())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .subtotal(item.getLineTotal() == null
                                ? item.getPrice().multiply(
                                        BigDecimal.valueOf(item.getQuantity()))
                                : item.getLineTotal())
                        .build())
                .toList();
        return OrderResponse.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .totalAmount(order.getTotalAmount())
                .subtotal(order.getSubtotal())
                .deliveryCharges(order.getDeliveryCharges())
                .discountAmount(order.getDiscountAmount())
                .currency(order.getCurrency())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .recipientName(order.getRecipientName())
                .phoneNumber(order.getPhoneNumber())
                .address(order.getAddress())
                .addressLine2(order.getAddressLine2())
                .landmark(order.getLandmark())
                .city(order.getCity())
                .state(order.getState())
                .pincode(order.getPincode())
                .country(order.getCountry())
                .createdAt(order.getCreatedAt())
                .items(items)
                .build();
    }
}
