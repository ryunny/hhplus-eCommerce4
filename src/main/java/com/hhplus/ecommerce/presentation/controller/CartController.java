package com.hhplus.ecommerce.presentation.controller;

import com.hhplus.ecommerce.application.usecase.CartUseCase;
import com.hhplus.ecommerce.domain.entity.CartItem;
import com.hhplus.ecommerce.presentation.dto.AddCartItemRequest;
import com.hhplus.ecommerce.presentation.dto.CartItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartUseCase cartUseCase;

    @PostMapping("/{userId}/items")
    public ResponseEntity<CartItemResponse> addToCart(
            @PathVariable Long userId,
            @RequestBody AddCartItemRequest request) {
        CartItem cartItem = cartUseCase.addToCart(userId, request.productId(), request.quantity());
        return ResponseEntity.ok(CartItemResponse.from(cartItem));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<CartItemResponse>> getCartItems(@PathVariable Long userId) {
        List<CartItem> cartItems = cartUseCase.getCartItems(userId);
        List<CartItemResponse> response = cartItems.stream()
                .map(CartItemResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<Void> removeFromCart(@PathVariable Long cartItemId) {
        cartUseCase.removeFromCart(cartItemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}/clear")
    public ResponseEntity<Void> clearCart(@PathVariable Long userId) {
        cartUseCase.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}
