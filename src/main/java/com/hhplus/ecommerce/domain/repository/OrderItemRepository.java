package com.hhplus.ecommerce.domain.repository;

import com.hhplus.ecommerce.domain.entity.OrderItem;

import java.util.List;
import java.util.Optional;

public interface OrderItemRepository {
    OrderItem save(OrderItem orderItem);
    Optional<OrderItem> findById(Long id);
    List<OrderItem> findByOrderId(Long orderId);
    void deleteById(Long id);
    List<OrderItem> getOrderItemByTopFive();
}
