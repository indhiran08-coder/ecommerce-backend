package com.indhiran.ecommerce.service;

import com.indhiran.ecommerce.dto.request.PlaceOrderRequest;
import com.indhiran.ecommerce.dto.response.OrderItemResponse;
import com.indhiran.ecommerce.dto.response.OrderResponse;
import com.indhiran.ecommerce.entity.*;
import com.indhiran.ecommerce.exception.ResourceNotFoundException;
import com.indhiran.ecommerce.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;

    // PLACE ORDER from cart
    @Transactional
    public OrderResponse placeOrder(String email,
                                    PlaceOrderRequest request) {

        // 1. Get user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "email", email));

        // 2. Get cart
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException(
                        "Cart is empty"));

        // 3. Get cart items
        List<CartItem> cartItems = cartItemRepository
                .findAll()
                .stream()
                .filter(item -> item.getCart()
                        .getId().equals(cart.getId()))
                .collect(Collectors.toList());

        if (cartItems.isEmpty()) {
            throw new RuntimeException(
                    "Cannot place order with empty cart");
        }

        // 4. Calculate total
        BigDecimal total = cartItems.stream()
                .map(item -> item.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(
                                item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 5. Create order
        Order order = Order.builder()
                .user(user)
                .totalAmount(total)
                .status(Order.OrderStatus.PENDING)
                .shippingAddress(request.getShippingAddress())
                .notes(request.getNotes())
                .build();

        Order savedOrder = orderRepository.save(order);

        // 6. Create order items with price snapshot
        List<OrderItem> orderItems = cartItems.stream()
                .map(cartItem -> OrderItem.builder()
                        .order(savedOrder)
                        .product(cartItem.getProduct())
                        .quantity(cartItem.getQuantity())
                        .unitPrice(cartItem.getProduct().getPrice())
                        .build())
                .collect(Collectors.toList());

        orderItemRepository.saveAll(orderItems);

        // 7. Clear cart after order placed
        cartItemRepository.deleteAll(cartItems);

        return mapToResponse(savedOrder, orderItems);
    }

    // GET MY ORDERS
    @Transactional(readOnly = true)
    public Page<OrderResponse> getMyOrders(
            String email, int page, int size) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "email", email));

        return orderRepository
                .findByUserOrderByPlacedAtDesc(
                        user, PageRequest.of(page, size))
                .map(order -> {
                    List<OrderItem> items =
                            orderItemRepository.findByOrder(order);
                    return mapToResponse(order, items);
                });
    }

    // GET ORDER BY ID
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(String email, Long orderId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "email", email));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order", "id", orderId));

        // Security check — order must belong to this user
        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException(
                    "Order does not belong to you");
        }

        List<OrderItem> items =
                orderItemRepository.findByOrder(order);
        return mapToResponse(order, items);
    }

    // CANCEL ORDER
    @Transactional
    public OrderResponse cancelOrder(String email, Long orderId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "email", email));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order", "id", orderId));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException(
                    "Order does not belong to you");
        }

        // Can only cancel PENDING or CONFIRMED orders
        if (order.getStatus() == Order.OrderStatus.SHIPPED
                || order.getStatus() == Order.OrderStatus.DELIVERED) {
            throw new RuntimeException(
                    "Cannot cancel order that is already "
                            + order.getStatus());
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        orderRepository.save(order);

        List<OrderItem> items =
                orderItemRepository.findByOrder(order);
        return mapToResponse(order, items);
    }

    // Map Order → OrderResponse
    private OrderResponse mapToResponse(
            Order order, List<OrderItem> items) {

        List<OrderItemResponse> itemResponses = items.stream()
                .map(item -> OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getUnitPrice()
                                .multiply(BigDecimal.valueOf(
                                        item.getQuantity())))
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .notes(order.getNotes())
                .items(itemResponses)
                .placedAt(order.getPlacedAt())
                .build();
    }

}