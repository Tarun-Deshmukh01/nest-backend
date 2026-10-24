package com.tarun.nest.service.impl;

import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import com.tarun.nest.dto.CreatePaymentRequest;
import com.tarun.nest.dto.CreateOrderRequest;
import com.tarun.nest.dto.CreatePaymentResponse;
import com.tarun.nest.dto.PaymentVerificationResponse;
import com.tarun.nest.dto.VerifyPaymentRequest;
import com.tarun.nest.entity.CustomerAddress;
import com.tarun.nest.entity.OrderItem;
import com.tarun.nest.entity.PaymentOrder;
import com.tarun.nest.entity.Product;
import com.tarun.nest.entity.User;
import com.tarun.nest.entity.VendorOrderFulfillment;
import com.tarun.nest.repository.CustomerAddressRepository;
import com.tarun.nest.repository.CartItemRepository;
import com.tarun.nest.repository.CartRepository;
import com.tarun.nest.repository.OrderRepository;
import com.tarun.nest.repository.PaymentOrderRepository;
import com.tarun.nest.repository.ProductRepository;
import com.tarun.nest.repository.UserRepository;
import com.tarun.nest.repository.VendorOrderFulfillmentRepository;
import com.tarun.nest.service.PaymentService;
import com.tarun.nest.service.OrderPaymentPreparationService;
import com.tarun.nest.service.OrderService;
import com.tarun.nest.service.RealtimeNotificationService;
import com.tarun.nest.enums.OrderPaymentStatus;
import com.tarun.nest.enums.OrderStatus;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final RazorpayClient razorpayClient;
    private final PaymentOrderRepository paymentRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CustomerAddressRepository addressRepository;
    private final OrderRepository orderRepository;
    private final VendorOrderFulfillmentRepository fulfillmentRepository;
    private final RealtimeNotificationService notificationService;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderPaymentPreparationService orderPaymentPreparationService;
    private final OrderService orderService;
    private final String keyId;
    private final String keySecret;
    private final String webhookSecret;

    public PaymentServiceImpl(
            RazorpayClient razorpayClient,
            PaymentOrderRepository paymentRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            CustomerAddressRepository addressRepository,
            OrderRepository orderRepository,
            VendorOrderFulfillmentRepository fulfillmentRepository,
            RealtimeNotificationService notificationService,
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            OrderPaymentPreparationService orderPaymentPreparationService,
            OrderService orderService,
            @Value("${razorpay.key-id:rzp_test_placeholder}") String keyId,
            @Value("${razorpay.key-secret:placeholder_secret}") String keySecret,
            @Value("${razorpay.webhook-secret:}") String webhookSecret
    ) {
        this.razorpayClient = razorpayClient;
        this.paymentRepository = paymentRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.orderRepository = orderRepository;
        this.fulfillmentRepository = fulfillmentRepository;
        this.notificationService = notificationService;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderPaymentPreparationService = orderPaymentPreparationService;
        this.orderService = orderService;
        this.keyId = keyId;
        this.keySecret = keySecret;
        this.webhookSecret = webhookSecret;
    }

    @Override
    public CreatePaymentResponse createCartPayment(
            CreateOrderRequest request,
            String customerEmail,
            String idempotencyKey) throws Exception {
        User customer = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Customer not found"
                ));
        com.tarun.nest.dto.OrderResponse order = orderService.createOrder(
                request, customer.getId(), idempotencyKey
        );
        return createPaymentForOrder(order.getOrderId(), customerEmail);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreatePaymentResponse createOrder(
            CreatePaymentRequest request,
            String customerEmail
    ) throws Exception {
        User customer = userRepository.findByEmailForUpdate(customerEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Customer not found"
                ));
        CustomerAddress deliveryAddress = resolveDeliveryAddress(
                request, customer
        );

        Product product = productRepository.findByIdForUpdate(request.productId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found"
                ));

        if (request.quantity() == null
                || request.quantity() < 1
                || request.quantity() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid quantity"
            );
        }

        BigDecimal unitPrice = product.getPrice();
        if (unitPrice == null || unitPrice.signum() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid product price"
            );
        }

        if (product.getStatus() != com.tarun.nest.entity.ProductStatus.PUBLISHED
                || product.getStock() == null
                || product.getStock() < request.quantity()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Product is unavailable or out of stock"
            );
        }
        unitPrice = product.getPrice();
        if (unitPrice == null || unitPrice.signum() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Product price is unavailable"
            );
        }

        com.tarun.nest.entity.Order applicationOrder = createSingleProductOrder(
                customer, deliveryAddress, product, request.quantity()
        );

        long amountPaise = unitPrice
                .multiply(BigDecimal.valueOf(request.quantity()))
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();

        PaymentOrder paymentOrder = new PaymentOrder();
        paymentOrder.setCustomerEmail(customerEmail);
        paymentOrder.setOrder(applicationOrder);
        paymentOrder.setProductId(product.getId());
        paymentOrder.setQuantity(request.quantity());
        paymentOrder.setDeliveryAddress(deliveryAddress);
        paymentOrder.setAmountPaise(amountPaise);
        paymentOrder = paymentRepository.saveAndFlush(paymentOrder);

        JSONObject options = new JSONObject();
        options.put("amount", amountPaise);
        options.put("currency", "INR");
        options.put(
                "receipt",
                "nest_" + UUID.randomUUID().toString().replace("-", "")
        );

        Order razorpayOrder;
        try {
            razorpayOrder = razorpayClient.orders.create(options);
        } catch (RazorpayException exception) {
            log.error(
                    "Razorpay order creation failed for internal order {} (provider code {})",
                    paymentOrder.getId(),
                    exception.getCode(),
                    exception
            );
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Payment gateway rejected the order request; verify the configured Razorpay key pair",
                    exception
            );
        }
        String razorpayOrderId = razorpayOrder.get("id");
        paymentOrder.setRazorpayOrderId(razorpayOrderId);
        paymentRepository.save(paymentOrder);

        return new CreatePaymentResponse(
                paymentOrder.getId(),
                razorpayOrderId,
                amountPaise,
                "INR",
                keyId,
                product.getName()
        );
    }

    private com.tarun.nest.entity.Order createSingleProductOrder(
            User customer,
            CustomerAddress address,
            Product product,
            int quantity) {
        BigDecimal lineTotal = product.getPrice().multiply(
                BigDecimal.valueOf(quantity));
        com.tarun.nest.entity.Order order = com.tarun.nest.entity.Order.builder()
                .user(customer)
                .orderNumber("NEST-" + UUID.randomUUID().toString()
                        .replace("-", "").substring(0, 16).toUpperCase())
                .status(OrderStatus.PENDING_PAYMENT)
                .paymentStatus(OrderPaymentStatus.CREATED)
                .recipientName(address.getRecipientName() == null
                        ? customer.getName() : address.getRecipientName())
                .phoneNumber(address.getPhoneNumber() == null
                        ? customer.getMobileNumber() : address.getPhoneNumber())
                .address(address.getAddress())
                .addressLine2(address.getAddressLine2())
                .landmark(address.getLandmark())
                .city(address.getCity())
                .state(address.getState())
                .pincode(address.getPincode())
                .country(address.getCountry())
                .subtotal(lineTotal)
                .deliveryCharges(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(lineTotal)
                .build();
        OrderItem item = OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(quantity)
                .price(product.getPrice())
                .productNameSnapshot(product.getName())
                .productImageSnapshot(product.getImageUrl())
                .vendorUserIdSnapshot(product.getVendor().getId())
                .vendorNameSnapshot(product.getVendor().getName())
                .lineTotal(lineTotal)
                .build();
        order.setItems(new java.util.ArrayList<>(List.of(item)));
        product.setStock(product.getStock() - quantity);
        com.tarun.nest.entity.Order savedOrder = orderRepository.saveAndFlush(order);
        VendorOrderFulfillment fulfillment = new VendorOrderFulfillment();
        fulfillment.setOrder(savedOrder);
        fulfillment.setVendor(product.getVendor());
        fulfillment.setStatus(OrderStatus.PENDING_PAYMENT);
        fulfillmentRepository.save(fulfillment);
        return savedOrder;
    }

    @Override
    public CreatePaymentResponse createPaymentForOrder(
            Long orderId,
            String customerEmail) throws Exception {
        OrderPaymentPreparationService.PreparedOrderPayment prepared =
                orderPaymentPreparationService.prepare(orderId, customerEmail);
        if (!prepared.createProviderOrder()) {
            return paymentResponse(prepared);
        }

        JSONObject options = new JSONObject();
        options.put("amount", prepared.amountPaise());
        options.put("currency", prepared.currency());
        options.put(
                "receipt",
                "nest_" + prepared.internalOrderId()
        );
        try {
            Order razorpayOrder = razorpayClient.orders.create(options);
            String razorpayOrderId = razorpayOrder.get("id");
            orderPaymentPreparationService.markCreated(
                    prepared.internalOrderId(), razorpayOrderId
            );
            return new CreatePaymentResponse(
                    prepared.internalOrderId(),
                    razorpayOrderId,
                    prepared.amountPaise(),
                    prepared.currency(),
                    keyId,
                    prepared.orderNumber()
            );
        } catch (RazorpayException exception) {
            orderPaymentPreparationService.markFailed(
                    prepared.internalOrderId()
            );
            log.error(
                    "Razorpay order creation failed for application order {} (provider code {})",
                    orderId,
                    exception.getCode(),
                    exception
            );
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Payment gateway could not create the payment order",
                    exception
            );
        }
    }

    private CreatePaymentResponse paymentResponse(
            OrderPaymentPreparationService.PreparedOrderPayment paymentOrder) {
        return new CreatePaymentResponse(
                paymentOrder.internalOrderId(),
                paymentOrder.razorpayOrderId(),
                paymentOrder.amountPaise(),
                paymentOrder.currency(),
                keyId,
                paymentOrder.orderNumber()
        );
    }

    private CustomerAddress resolveDeliveryAddress(
            CreatePaymentRequest request,
            User customer) {
        if (request.addressId() != null && request.deliveryAddress() != null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Provide either addressId or deliveryAddress, not both"
            );
        }

        if (request.deliveryAddress() != null) {
            if (addressRepository.countByCustomerId(customer.getId()) >= 5) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "A customer can save at most five addresses"
                );
            }

            CustomerAddress address = new CustomerAddress();
            address.setCustomer(customer);
            address.setLabel(trimToNull(request.deliveryAddress().label()));
            address.setRecipientName(
                    trimToNull(request.deliveryAddress().recipientName()));
            address.setPhoneNumber(
                    trimToNull(request.deliveryAddress().phoneNumber()));
            address.setAddress(request.deliveryAddress().address().trim());
            address.setAddressLine2(
                    trimToNull(request.deliveryAddress().addressLine2()));
            address.setLandmark(
                    trimToNull(request.deliveryAddress().landmark()));
            address.setCity(request.deliveryAddress().city().trim());
            address.setState(trimToNull(request.deliveryAddress().state()));
            address.setPincode(request.deliveryAddress().pincode().trim());
            address.setCountry(request.deliveryAddress().country().trim());
            address.setDefaultAddress(
                    addressRepository.countByCustomerId(customer.getId()) == 0
                            || Boolean.TRUE.equals(
                                    request.deliveryAddress().defaultAddress())
            );
            if (address.isDefaultAddress()) {
                List<CustomerAddress> existingDefaults =
                        addressRepository.findByCustomerIdAndDefaultAddressTrue(
                                customer.getId()
                        );
                existingDefaults.forEach(existing ->
                        existing.setDefaultAddress(false)
                );
                addressRepository.saveAll(existingDefaults);
            }
            return addressRepository.saveAndFlush(address);
        }

        if (request.addressId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Provide a saved addressId or a deliveryAddress"
            );
        }

        return addressRepository
                .findByIdAndCustomerId(request.addressId(), customer.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Select a valid delivery address before starting payment"
                ));
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentVerificationResponse verifyPayment(
            VerifyPaymentRequest request,
            String customerEmail
    ) throws Exception {
        PaymentOrder savedOrder = paymentRepository.findByIdAndCustomerEmailForUpdate(
                        request.internalOrderId(), customerEmail
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Payment order not found"
                ));

        if (savedOrder.getRazorpayOrderId() == null
                || !savedOrder.getRazorpayOrderId()
                        .equals(request.razorpayOrderId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Order ID mismatch"
            );
        }

        if (savedOrder.getStatus() == PaymentOrder.PaymentStatus.PAID) {
            if (request.razorpayPaymentId().equals(
                    savedOrder.getRazorpayPaymentId())) {
                return new PaymentVerificationResponse(
                        true, "PAID", "Payment already verified"
                );
            }
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Order already paid"
            );
        }

        if (savedOrder.getStatus() == PaymentOrder.PaymentStatus.FAILED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Payment order is closed"
            );
        }

        JSONObject attributes = new JSONObject();
        attributes.put("razorpay_order_id", savedOrder.getRazorpayOrderId());
        attributes.put("razorpay_payment_id", request.razorpayPaymentId());
        attributes.put("razorpay_signature", request.razorpaySignature());

        boolean signatureValid;
        try {
            signatureValid =
                    Utils.verifyPaymentSignature(attributes, keySecret);
        } catch (RazorpayException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid payment signature", exception
            );
        }

        if (!signatureValid) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid payment signature"
            );
        }

        Payment razorpayPayment;
        try {
            razorpayPayment =
                    razorpayClient.payments.fetch(request.razorpayPaymentId());
        } catch (RazorpayException exception) {
            log.error(
                    "Razorpay payment lookup failed for internal order {} (provider code {})",
                    savedOrder.getId(),
                    exception.getCode(),
                    exception
            );
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Payment gateway could not verify the payment",
                    exception
            );
        }
        if (!savedOrder.getRazorpayOrderId().equals(
                razorpayPayment.get("order_id"))) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Payment belongs to another order"
            );
        }

        long verifiedAmount = ((Number) razorpayPayment.get("amount"))
                .longValue();
        if (verifiedAmount != savedOrder.getAmountPaise()
                || !"INR".equals(razorpayPayment.get("currency"))) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Payment amount mismatch"
            );
        }

        String status = razorpayPayment.get("status");
        savedOrder.setRazorpayPaymentId(request.razorpayPaymentId());

        if ("captured".equals(status)) {
            savedOrder.setStatus(PaymentOrder.PaymentStatus.PAID);
            paymentRepository.save(savedOrder);
            confirmApplicationOrder(savedOrder);
            return new PaymentVerificationResponse(
                    true, "PAID", "Payment verified successfully"
            );
        }

        if ("authorized".equals(status)) {
            savedOrder.setStatus(PaymentOrder.PaymentStatus.VERIFIED);
            paymentRepository.save(savedOrder);
            return new PaymentVerificationResponse(
                    false, "AUTHORIZED",
                    "Payment authorized but not captured yet"
            );
        }

        if ("failed".equals(status)) {
            savedOrder.setStatus(PaymentOrder.PaymentStatus.FAILED);
            paymentRepository.save(savedOrder);
            markPaymentFailed(savedOrder);
            return new PaymentVerificationResponse(
                    false, "FAILED", "Payment failed; the order remains available for payment retry"
            );
        }

        paymentRepository.save(savedOrder);
        return new PaymentVerificationResponse(
                false, status, "Payment is not captured"
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleWebhook(String payload, String signature) throws Exception {
        if (webhookSecret.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Payment webhook secret is not configured"
            );
        }
        boolean signatureValid;
        try {
            signatureValid = Utils.verifyWebhookSignature(
                    payload, signature, webhookSecret
            );
        } catch (RazorpayException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid webhook signature", exception
            );
        }
        if (!signatureValid) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid webhook signature"
            );
        }

        JSONObject event = new JSONObject(payload);
        String eventName = event.optString("event");
        if (!"payment.captured".equals(eventName)
                && !"payment.failed".equals(eventName)) {
            return;
        }
        JSONObject paymentEntity = event.getJSONObject("payload")
                .getJSONObject("payment")
                .getJSONObject("entity");
        String providerOrderId = paymentEntity.getString("order_id");
        String providerPaymentId = paymentEntity.getString("id");
        PaymentOrder savedOrder = paymentRepository
                .findByRazorpayOrderIdForUpdate(providerOrderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Payment order not found"
                ));
        if (savedOrder.getStatus() == PaymentOrder.PaymentStatus.PAID) {
            if (providerPaymentId.equals(savedOrder.getRazorpayPaymentId())) {
                return;
            }
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Payment order was already completed with another payment"
            );
        }
        Payment providerPayment;
        try {
            providerPayment = razorpayClient.payments.fetch(providerPaymentId);
        } catch (RazorpayException exception) {
            log.error(
                    "Razorpay webhook payment lookup failed for internal order {} (provider code {})",
                    savedOrder.getId(),
                    exception.getCode(),
                    exception
            );
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Payment gateway could not verify the webhook payment",
                    exception
            );
        }
        if (!providerOrderId.equals(providerPayment.get("order_id"))
                || ((Number) providerPayment.get("amount")).longValue()
                        != savedOrder.getAmountPaise()
                || !savedOrder.getCurrency().equals(
                        providerPayment.get("currency"))) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Webhook payment details mismatch"
            );
        }

        savedOrder.setRazorpayPaymentId(providerPaymentId);
        if ("payment.captured".equals(eventName)
                && "captured".equals(providerPayment.get("status"))) {
            savedOrder.setStatus(PaymentOrder.PaymentStatus.PAID);
            paymentRepository.save(savedOrder);
            confirmApplicationOrder(savedOrder);
            return;
        }
        if ("payment.failed".equals(eventName)
                && "failed".equals(providerPayment.get("status"))) {
            savedOrder.setStatus(PaymentOrder.PaymentStatus.FAILED);
            paymentRepository.save(savedOrder);
            markPaymentFailed(savedOrder);
        }
    }

    private void confirmApplicationOrder(PaymentOrder paymentOrder) {
        if (paymentOrder.getOrder() == null) {
            return;
        }
        com.tarun.nest.entity.Order order = orderRepository.findById(
                        paymentOrder.getOrder().getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Application order not found"
                ));
        if (order.getPaymentStatus() == OrderPaymentStatus.PAID) {
            return;
        }
        order.setPaymentStatus(OrderPaymentStatus.PAID);
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);
        cartRepository.findByUserId(order.getUser().getId()).ifPresent(cart ->
                order.getItems().forEach(orderItem ->
                        cartItemRepository.findByCartIdAndProductId(
                                cart.getId(), orderItem.getProduct().getId()
                        ).ifPresent(cartItem -> {
                            int remainingQuantity = cartItem.getQuantity()
                                    - orderItem.getQuantity();
                            if (remainingQuantity > 0) {
                                cartItem.setQuantity(remainingQuantity);
                                cartItemRepository.save(cartItem);
                            } else {
                                cartItemRepository.delete(cartItem);
                            }
                        })
                )
        );
        Map<Long, List<String>> productsByVendor = order.getItems().stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        item -> item.getVendorUserIdSnapshot() == null
                                ? item.getProduct().getVendor().getId()
                                : item.getVendorUserIdSnapshot(),
                        LinkedHashMap::new,
                        java.util.stream.Collectors.mapping(
                                item -> item.getProductNameSnapshot() == null
                                        ? item.getProduct().getName()
                                        : item.getProductNameSnapshot(),
                                java.util.stream.Collectors.toList()
                        )
                ));
        productsByVendor.forEach((vendorId, productNames) -> {
            String productList = String.join(", ", productNames);
            notificationService.sendNotificationToUsers(
                    List.of(vendorId),
                    "New paid order",
                    order.getUser().getName() + " ordered " + productList
                            + " (order " + order.getOrderNumber()
                            + "). Ready for fulfillment.",
                    "/vendor/orders"
            );
        });
        notificationService.sendNotificationToUsers(
                List.of(order.getUser().getId()),
                "Order confirmed",
                "Your order " + order.getOrderNumber() + " has been confirmed.",
                "/orders"
        );
    }

    private void markPaymentFailed(PaymentOrder paymentOrder) {
        if (paymentOrder.getOrder() == null) {
            return;
        }
        PaymentOrder latestAttempt = paymentRepository
                .findFirstByOrderIdOrderByIdDesc(paymentOrder.getOrder().getId())
                .orElse(paymentOrder);
        if (!latestAttempt.getId().equals(paymentOrder.getId())) {
            return;
        }
        com.tarun.nest.entity.Order order = orderRepository.findById(
                        paymentOrder.getOrder().getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Application order not found"
                ));
        if (order.getPaymentStatus() == OrderPaymentStatus.PAID) {
            return;
        }
        order.setPaymentStatus(OrderPaymentStatus.FAILED);
        orderRepository.save(order);
    }
}
