package com.hhplus.ecommerce.test.mocks;

import com.hhplus.ecommerce.domain.entity.OrderItem;
import com.hhplus.ecommerce.domain.repository.OrderItemRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class MockOrderItemRepository implements OrderItemRepository {

    private final Map<Long, OrderItem> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public OrderItem save(OrderItem orderItem) {
        if (orderItem.getId() == null) {
            setId(orderItem, idGenerator.getAndIncrement());
        }
        store.put(orderItem.getId(), orderItem);
        return orderItem;
    }

    @Override
    public Optional<OrderItem> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<OrderItem> findByOrderId(Long orderId) {
        return store.values().stream()
                .filter(item -> item.getOrder().getId().equals(orderId))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        store.remove(id);
    }

    @Override
    public List<OrderItem> getOrderItemByTopFive() {
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);

        // 최근 3일간의 주문만 필터링하고 상품별로 총 판매 수량을 계산
        Map<Long, Integer> productSales = store.values().stream()
                .filter(item -> item.getOrder().getCreatedAt().isAfter(threeDaysAgo))
                .collect(Collectors.groupingBy(
                        item -> item.getProduct().getId(),
                        Collectors.summingInt(item -> item.getQuantity().getValue())
                ));

        // 판매량 기준으로 정렬 후 상위 5개 상품 ID 추출
        List<Long> topProductIds = productSales.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .toList();

        // 그 상품들의 OrderItem만 반환 (최근 3일 내)
        return store.values().stream()
                .filter(item -> topProductIds.contains(item.getProduct().getId()))
                .filter(item -> item.getOrder().getCreatedAt().isAfter(threeDaysAgo))
                .toList();
    }

    private void setId(OrderItem orderItem, Long id) {
        try {
            java.lang.reflect.Field idField = OrderItem.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(orderItem, id);
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
