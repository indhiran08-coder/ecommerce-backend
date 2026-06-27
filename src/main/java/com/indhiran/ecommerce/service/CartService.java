package com.indhiran.ecommerce.service;

import com.indhiran.ecommerce.dto.request.CartItemRequest;
import com.indhiran.ecommerce.dto.response.CartItemResponse;
import com.indhiran.ecommerce.dto.response.CartResponse;
import com.indhiran.ecommerce.entity.Cart;
import com.indhiran.ecommerce.entity.CartItem;
import com.indhiran.ecommerce.entity.Product;
import com.indhiran.ecommerce.entity.User;
import com.indhiran.ecommerce.exception.ResourceNotFoundException;
import com.indhiran.ecommerce.repository.CartItemRepository;
import com.indhiran.ecommerce.repository.CartRepository;
import com.indhiran.ecommerce.repository.ProductRepository;
import com.indhiran.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    // GET or CREATE cart for user
    @Transactional
    private Cart getOrCreateCart(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "email", email));

        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .user(user)
                            .build();
                    return cartRepository.save(newCart);
                });
    }

    // GET CART
    @Transactional
    public CartResponse getCart(String email) {
        Cart cart = getOrCreateCart(email);
        return mapToResponse(cart);
    }

    // ADD ITEM
    @Transactional
    public CartResponse addItem(String email,
                                CartItemRequest request) {

        Cart cart = getOrCreateCart(email);

        Product product = productRepository
                .findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product", "id", request.getProductId()));

        if (product.getStock() < request.getQuantity()) {
            throw new RuntimeException(
                    "Insufficient stock. Available: "
                            + product.getStock());
        }

        CartItem cartItem = cartItemRepository
                .findByCartAndProduct(cart, product)
                .map(existing -> {
                    existing.setQuantity(
                            existing.getQuantity()
                                    + request.getQuantity());
                    return existing;
                })
                .orElse(CartItem.builder()
                        .cart(cart)
                        .product(product)
                        .quantity(request.getQuantity())
                        .build());

        cartItemRepository.save(cartItem);

        cart = cartRepository.findById(cart.getId())
                .orElseThrow();

        return mapToResponse(cart);
    }

    // UPDATE ITEM QUANTITY
    @Transactional
    public CartResponse updateItem(String email,
                                   Long itemId,
                                   CartItemRequest request) {

        Cart cart = getOrCreateCart(email);

        CartItem cartItem = cartItemRepository
                .findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "CartItem", "id", itemId));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException(
                    "Item does not belong to your cart");
        }

        if (cartItem.getProduct().getStock() < request.getQuantity()) {
            throw new RuntimeException(
                    "Insufficient stock. Available: "
                            + cartItem.getProduct().getStock());
        }

        cartItem.setQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);

        cart = cartRepository.findById(cart.getId()).orElseThrow();
        return mapToResponse(cart);
    }

    // REMOVE ITEM
    @Transactional
    public CartResponse removeItem(String email, Long itemId) {

        Cart cart = getOrCreateCart(email);

        CartItem cartItem = cartItemRepository
                .findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "CartItem", "id", itemId));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException(
                    "Item does not belong to your cart");
        }

        cartItemRepository.delete(cartItem);

        cart = cartRepository.findById(cart.getId()).orElseThrow();
        return mapToResponse(cart);
    }

    // CLEAR CART
    @Transactional
    public void clearCart(String email) {
        Cart cart = getOrCreateCart(email);
        cartItemRepository.deleteAll(
                cartItemRepository.findAll()
                        .stream()
                        .filter(item -> item.getCart()
                                .getId().equals(cart.getId()))
                        .collect(Collectors.toList()));
    }

    // Map Cart → CartResponse
    private CartResponse mapToResponse(Cart cart) {

        List<CartItemResponse> items = new ArrayList<>();

        if (cart.getId() != null) {
            items = cartItemRepository.findAll()
                    .stream()
                    .filter(item -> item.getCart()
                            .getId().equals(cart.getId()))
                    .map(this::mapItemToResponse)
                    .collect(Collectors.toList());
        }

        BigDecimal totalPrice = items.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .cartId(cart.getId())
                .items(items)
                .totalPrice(totalPrice)
                .totalItems(items.size())
                .build();
    }

    // Map CartItem → CartItemResponse
    private CartItemResponse mapItemToResponse(CartItem item) {
        BigDecimal subtotal = item.getProduct().getPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));

        return CartItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .productPrice(item.getProduct().getPrice())
                .quantity(item.getQuantity())
                .subtotal(subtotal)
                .build();
    }

}