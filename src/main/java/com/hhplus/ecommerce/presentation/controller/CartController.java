package com.hhplus.ecommerce.presentation.controller;

import com.hhplus.ecommerce.application.command.AddToCartCommand;
import com.hhplus.ecommerce.application.query.GetCartItemsQuery;
import com.hhplus.ecommerce.application.usecase.cart.AddToCartUseCase;
import com.hhplus.ecommerce.application.usecase.cart.ClearCartUseCase;
import com.hhplus.ecommerce.application.usecase.cart.GetCartItemsUseCase;
import com.hhplus.ecommerce.application.usecase.cart.RemoveFromCartUseCase;
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

    private final AddToCartUseCase addToCartUseCase;
    private final GetCartItemsUseCase getCartItemsUseCase;
    private final RemoveFromCartUseCase removeFromCartUseCase;
    private final ClearCartUseCase clearCartUseCase;

    @PostMapping("/{userId}/items")
    public ResponseEntity<CartItemResponse> addToCart(
            @PathVariable Long userId,
            @RequestBody AddCartItemRequest request) {
        AddToCartCommand command = new AddToCartCommand(userId, request.productId(), request.quantity());
        CartItem cartItem = addToCartUseCase.execute(command);
        return ResponseEntity.ok(CartItemResponse.from(cartItem));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<CartItemResponse>> getCartItems(@PathVariable Long userId) {
        GetCartItemsQuery query = new GetCartItemsQuery(userId);
        List<CartItem> cartItems = getCartItemsUseCase.execute(query);
        List<CartItemResponse> response = cartItems.stream()
                .map(CartItemResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<Void> removeFromCart(@PathVariable Long cartItemId) {
        removeFromCartUseCase.execute(cartItemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}/clear")
    public ResponseEntity<Void> clearCart(@PathVariable Long userId) {
        clearCartUseCase.execute(userId);
        return ResponseEntity.noContent().build();
    }
}
