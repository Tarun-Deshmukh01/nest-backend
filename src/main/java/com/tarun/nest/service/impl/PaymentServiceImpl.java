package com.tarun.nest.service.impl;

import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import com.tarun.nest.dto.CreatePaymentRequest;
import com.tarun.nest.dto.CreatePaymentResponse;
import com.tarun.nest.dto.PaymentVerificationResponse;
import com.tarun.nest.dto.VerifyPaymentRequest;
import com.tarun.nest.entity.PaymentOrder;
import com.tarun.nest.entity.Product;
import com.tarun.nest.repository.PaymentOrderRepository;
import com.tarun.nest.repository.ProductRepository;
import com.tarun.nest.service.PaymentService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final RazorpayClient razorpayClient;
    private final PaymentOrderRepository paymentRepository;
    private final ProductRepository productRepository;
    private final String keyId;
    private final String keySecret;

    public PaymentServiceImpl(
            RazorpayClient razorpayClient,
            PaymentOrderRepository paymentRepository,
            ProductRepository productRepository,
            @Value("${razorpay.key-id}") String keyId,
            @Value("${razorpay.key-secret}") String keySecret
    ) {
        this.razorpayClient = razorpayClient;
        this.paymentRepository = paymentRepository;
        this.productRepository = productRepository;
        this.keyId = keyId;
        this.keySecret = keySecret;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreatePaymentResponse createOrder(
            CreatePaymentRequest request,
            String customerEmail
    ) throws Exception {
        Product product = productRepository.findById(request.productId())
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

        long amountPaise = unitPrice
                .multiply(BigDecimal.valueOf(request.quantity()))
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();

        PaymentOrder paymentOrder = new PaymentOrder();
        paymentOrder.setCustomerEmail(customerEmail);
        paymentOrder.setProductId(product.getId());
        paymentOrder.setQuantity(request.quantity());
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentVerificationResponse verifyPayment(
            VerifyPaymentRequest request,
            String customerEmail
    ) throws Exception {
        PaymentOrder savedOrder = paymentRepository.findByIdAndCustomerEmail(
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

        paymentRepository.save(savedOrder);
        return new PaymentVerificationResponse(
                false, status, "Payment is not captured"
        );
    }
}
