package com.hhplus.ecommerce.domain.repository;

import com.hhplus.ecommerce.domain.entity.Payment;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository {
    Payment save(Payment payment);
    Optional<Payment> findById(Long id);
    Optional<Payment> findByOrderId(Long orderId);
    List<Payment> findAll();
    void deleteById(Long id);
}
