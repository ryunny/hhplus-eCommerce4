package com.hhplus.ecommerce.presentation.dto;

import com.hhplus.ecommerce.domain.entity.CartItem;

public record CartItemResponse(
        Long id,
        Long productId,
        String productName,
        Long price,
        Integer quantity
) {
    public static CartItemResponse from(CartItem cartItem) {
        return new CartItemResponse(
                cartItem.getId(),
                cartItem.getProduct().getId(),
                cartItem.getProduct().getName(),
                cartItem.getProduct().getPrice().getAmount(),
                cartItem.getQuantity().getValue()
        );
    }
}
