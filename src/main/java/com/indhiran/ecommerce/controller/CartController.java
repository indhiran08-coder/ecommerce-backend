package com.indhiran.ecommerce.controller;

import com.indhiran.ecommerce.dto.request.CartItemRequest;
import com.indhiran.ecommerce.dto.response.ApiResponse;
import com.indhiran.ecommerce.dto.response.CartResponse;
import com.indhiran.ecommerce.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(ApiResponse.success(
                "Cart fetched",
                cartService.getCart(userDetails.getUsername())));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CartItemRequest request) {

        return ResponseEntity.ok(ApiResponse.success(
                "Item added to cart",
                cartService.addItem(
                        userDetails.getUsername(), request)));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long itemId,
            @Valid @RequestBody CartItemRequest request) {

        return ResponseEntity.ok(ApiResponse.success(
                "Cart item updated",
                cartService.updateItem(
                        userDetails.getUsername(),
                        itemId, request)));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long itemId) {

        return ResponseEntity.ok(ApiResponse.success(
                "Item removed from cart",
                cartService.removeItem(
                        userDetails.getUsername(), itemId)));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @AuthenticationPrincipal UserDetails userDetails) {

        cartService.clearCart(userDetails.getUsername());
        return ResponseEntity.ok(
                ApiResponse.success("Cart cleared", null));
    }

}