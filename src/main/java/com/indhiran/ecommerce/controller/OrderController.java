package com.indhiran.ecommerce.controller;

import com.indhiran.ecommerce.dto.request.PlaceOrderRequest;
import com.indhiran.ecommerce.dto.response.ApiResponse;
import com.indhiran.ecommerce.dto.response.OrderResponse;
import com.indhiran.ecommerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody PlaceOrderRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Order placed successfully",
                        orderService.placeOrder(
                                userDetails.getUsername(),
                                request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderResponse>>>
    getMyOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(ApiResponse.success(
                "Orders fetched",
                orderService.getMyOrders(
                        userDetails.getUsername(), page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>>
    getOrderById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        return ResponseEntity.ok(ApiResponse.success(
                "Order fetched",
                orderService.getOrderById(
                        userDetails.getUsername(), id)));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        return ResponseEntity.ok(ApiResponse.success(
                "Order cancelled",
                orderService.cancelOrder(
                        userDetails.getUsername(), id)));
    }

}