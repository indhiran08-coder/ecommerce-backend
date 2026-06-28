package com.indhiran.ecommerce.service;

import com.indhiran.ecommerce.dto.request.PaymentRequest;
import com.indhiran.ecommerce.dto.response.PaymentResponse;
import com.indhiran.ecommerce.entity.Order;
import com.indhiran.ecommerce.entity.Payment;
import com.indhiran.ecommerce.exception.ResourceNotFoundException;
import com.indhiran.ecommerce.repository.OrderRepository;
import com.indhiran.ecommerce.repository.PaymentRepository;
import com.indhiran.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    // PROCESS PAYMENT
    @Transactional
    public PaymentResponse processPayment(
            String email, Long orderId,
            PaymentRequest request) {

        // 1. Get order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order", "id", orderId));

        // 2. Security check
        if (!order.getUser().getEmail().equals(email)) {
            throw new RuntimeException(
                    "Order does not belong to you");
        }

        // 3. Check order status
        if (order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new RuntimeException(
                    "Cannot pay for a cancelled order");
        }

        // 4. Check if already paid
        paymentRepository.findByOrderId(orderId).ifPresent(p -> {
            if (p.getStatus() == Payment.PaymentStatus.SUCCESS) {
                throw new RuntimeException(
                        "Order is already paid");
            }
        });

        // 5. Simulate payment gateway
        // In real world: call Stripe/PayPal/Razorpay API here
        boolean paymentSuccess = simulatePaymentGateway(request);

        // 6. Create payment record
        Payment payment = Payment.builder()
                .order(order)
                .amount(order.getTotalAmount())
                .status(paymentSuccess
                        ? Payment.PaymentStatus.SUCCESS
                        : Payment.PaymentStatus.FAILED)
                .transactionId(UUID.randomUUID().toString())
                .build();

        Payment saved = paymentRepository.save(payment);

        // 7. Update order status if payment successful
        if (paymentSuccess) {
            order.setStatus(Order.OrderStatus.CONFIRMED);
            orderRepository.save(order);
        }

        return mapToResponse(saved,
                paymentSuccess
                        ? "Payment successful"
                        : "Payment failed. Please try again.");
    }

    // GET PAYMENT STATUS
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentStatus(
            String email, Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order", "id", orderId));

        if (!order.getUser().getEmail().equals(email)) {
            throw new RuntimeException(
                    "Order does not belong to you");
        }

        Payment payment = paymentRepository
                .findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment", "orderId", orderId));

        return mapToResponse(payment, "Payment status fetched");
    }

    // REFUND
    @Transactional
    public PaymentResponse refundPayment(
            String email, Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order", "id", orderId));

        if (!order.getUser().getEmail().equals(email)) {
            throw new RuntimeException(
                    "Order does not belong to you");
        }

        Payment payment = paymentRepository
                .findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment", "orderId", orderId));

        if (payment.getStatus() != Payment.PaymentStatus.SUCCESS) {
            throw new RuntimeException(
                    "Only successful payments can be refunded");
        }

        // Process refund
        payment.setStatus(Payment.PaymentStatus.REFUNDED);
        paymentRepository.save(payment);

        // Cancel the order
        order.setStatus(Order.OrderStatus.CANCELLED);
        orderRepository.save(order);

        return mapToResponse(payment, "Refund processed successfully");
    }

    // Mock payment gateway — simulates 90% success rate
    private boolean simulatePaymentGateway(PaymentRequest request) {
        // Simulate failed payment for test card
        if (request.getCardNumber() != null
                && request.getCardNumber().equals("0000000000000000")) {
            return false;
        }
        // 90% success rate simulation
        return Math.random() > 0.1;
    }

    // Map Payment → PaymentResponse
    private PaymentResponse mapToResponse(
            Payment payment, String message) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrder().getId())
                .amount(payment.getAmount())
                .status(payment.getStatus().name())
                .transactionId(payment.getTransactionId())
                .message(message)
                .paidAt(payment.getPaidAt())
                .build();
    }

}