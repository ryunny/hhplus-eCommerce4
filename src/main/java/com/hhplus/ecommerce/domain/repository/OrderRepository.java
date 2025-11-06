package com.hhplus.ecommerce.domain.repository;

import com.hhplus.ecommerce.domain.entity.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(Long id);
    List<Order> findByUserId(Long userId);
    List<Order> findAll();
    void deleteById(Long id);
}
