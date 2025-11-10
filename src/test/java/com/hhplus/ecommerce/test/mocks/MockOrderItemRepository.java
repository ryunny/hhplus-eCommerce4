package com.hhplus.ecommerce.test.mocks;

import com.hhplus.ecommerce.domain.dto.ProductSalesDto;
import com.hhplus.ecommerce.domain.entity.OrderItem;
import com.hhplus.ecommerce.domain.entity.Product;
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
    public List<ProductSalesDto> getTopSellingProducts(LocalDateTime threeDaysAgo, int limit) {
        // 최근 3일간의 주문 항목을 필터링하고 상품별로 판매 수량 집계
        Map<Product, Integer> productSales = store.values().stream()
                .filter(item -> item.getOrder().getCreatedAt().isAfter(threeDaysAgo))
                .collect(Collectors.groupingBy(
                        OrderItem::getProduct,
                        Collectors.summingInt(item -> item.getQuantity().getValue())
                ));

        // 판매량 기준 내림차순 정렬 후 상위 N개 추출하여 DTO로 변환
        return productSales.entrySet().stream()
                .sorted(Map.Entry.<Product, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> ProductSalesDto.of(entry.getKey(), entry.getValue()))
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
