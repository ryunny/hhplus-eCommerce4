package com.hhplus.ecommerce.test.mocks;

import com.hhplus.ecommerce.domain.entity.Payment;
import com.hhplus.ecommerce.domain.repository.PaymentRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class MockPaymentRepository implements PaymentRepository {

    private final Map<Long, Payment> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Payment save(Payment payment) {
        if (payment.getId() == null) {
            setId(payment, idGenerator.getAndIncrement());
        }
        store.put(payment.getId(), payment);
        return payment;
    }

    @Override
    public Optional<Payment> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<Payment> findByOrderId(Long orderId) {
        return store.values().stream()
                .filter(payment -> payment.getOrder().getId().equals(orderId))
                .findFirst();
    }

    @Override
    public Optional<Payment> findByPaymentId(String paymentId) {
        return store.values().stream()
                .filter(payment -> payment.getPaymentId().equals(paymentId))
                .findFirst();
    }

    @Override
    public void deleteById(Long id) {
        store.remove(id);
    }

    private void setId(Payment payment, Long id) {
        try {
            java.lang.reflect.Field idField = Payment.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(payment, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set id", e);
        }
    }

    public void clear() {
        store.clear();
        idGenerator.set(1);
    }

    public int size() {
        return store.size();
    }
}
