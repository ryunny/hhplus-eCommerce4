package com.hhplus.ecommerce.test.mocks;

import com.hhplus.ecommerce.domain.entity.CartItem;
import com.hhplus.ecommerce.domain.entity.Product;
import com.hhplus.ecommerce.domain.entity.User;
import com.hhplus.ecommerce.domain.repository.CartItemRepository;
import com.hhplus.ecommerce.domain.vo.Quantity;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class MockCartItemRepository implements CartItemRepository {

    private final Map<Long, CartItem> cartItems = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public MockCartItemRepository() {
        // 초기 테스트 데이터는 별도로 추가 가능
        idGenerator.set(1);
    }

    @Override
    public CartItem save(CartItem cartItem) {
        if (cartItem.getId() == null) {
            setId(cartItem, idGenerator.getAndIncrement());
            setCreatedAt(cartItem, LocalDateTime.now());
        }
        cartItems.put(cartItem.getId(), cartItem);
        return cartItem;
    }

    @Override
    public Optional<CartItem> findById(Long id) {
        return Optional.ofNullable(cartItems.get(id));
    }

    @Override
    public List<CartItem> findByUserId(Long userId) {
        return cartItems.values().stream()
                .filter(item -> item.getUser().getId().equals(userId))
                .toList();
    }

    @Override
    public Optional<CartItem> findByUserIdAndProductId(Long userId, Long productId) {
        return cartItems.values().stream()
                .filter(item -> item.getUser().getId().equals(userId)
                        && item.getProduct().getId().equals(productId))
                .findFirst();
    }

    @Override
    public void deleteById(Long id) {
        cartItems.remove(id);
    }

    @Override
    public void deleteByUserId(Long userId) {
        List<Long> idsToDelete = cartItems.values().stream()
                .filter(item -> item.getUser().getId().equals(userId))
                .map(CartItem::getId)
                .toList();

        idsToDelete.forEach(cartItems::remove);
    }

    // Helper methods for creating test data
    public CartItem createCartItem(Long id, User user, Product product, Integer quantity) {
        CartItem cartItem = new CartItem(user, product, new Quantity(quantity));
        setId(cartItem, id);
        setCreatedAt(cartItem, LocalDateTime.now());
        return cartItem;
    }

    private void setId(CartItem cartItem, Long id) {
        try {
            java.lang.reflect.Field idField = CartItem.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(cartItem, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set id", e);
        }
    }

    private void setCreatedAt(CartItem cartItem, LocalDateTime createdAt) {
        try {
            java.lang.reflect.Field createdAtField = CartItem.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(cartItem, createdAt);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set createdAt", e);
        }
    }

    // Test helper methods
    public void clear() {
        cartItems.clear();
        idGenerator.set(1);
    }

    public int size() {
        return cartItems.size();
    }
}
