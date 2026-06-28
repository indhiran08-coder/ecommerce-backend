package com.indhiran.ecommerce.controller;

import com.indhiran.ecommerce.dto.request.PaymentRequest;
import com.indhiran.ecommerce.dto.response.ApiResponse;
import com.indhiran.ecommerce.dto.response.PaymentResponse;
import com.indhiran.ecommerce.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{orderId}/pay")
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId,
            @RequestBody PaymentRequest request) {

        return ResponseEntity.ok(ApiResponse.success(
                "Payment processed",
                paymentService.processPayment(
                        userDetails.getUsername(),
                        orderId, request)));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<PaymentResponse>>
    getPaymentStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId) {

        return ResponseEntity.ok(ApiResponse.success(
                "Payment status",
                paymentService.getPaymentStatus(
                        userDetails.getUsername(), orderId)));
    }

    @PostMapping("/{orderId}/refund")
    public ResponseEntity<ApiResponse<PaymentResponse>> refundPayment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId) {

        return ResponseEntity.ok(ApiResponse.success(
                "Refund processed",
                paymentService.refundPayment(
                        userDetails.getUsername(), orderId)));
    }

}