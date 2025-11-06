package com.hhplus.ecommerce.presentation.dto;

public record AddCartItemRequest(
        Long productId,
        Integer quantity
) {
}
