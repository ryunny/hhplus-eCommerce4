package com.hhplus.ecommerce.presentation.dto;

import java.util.List;

public record CreateOrderRequest(
        List<OrderItemRequest> items,
        Long userCouponId,
        String recipientName,
        String shippingAddress,
        String shippingPhone
) {
    public record OrderItemRequest(
            Long productId,
            Integer quantity
    ) {
    }
}
