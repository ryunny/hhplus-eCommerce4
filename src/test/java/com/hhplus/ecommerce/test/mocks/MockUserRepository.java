package com.hhplus.ecommerce.test.mocks;

import com.hhplus.ecommerce.domain.entity.User;
import com.hhplus.ecommerce.domain.repository.UserRepository;
import com.hhplus.ecommerce.domain.vo.Email;
import com.hhplus.ecommerce.domain.vo.Money;
import com.hhplus.ecommerce.domain.vo.Phone;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class MockUserRepository implements UserRepository {

    private final Map<Long, User> users = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public MockUserRepository() {
        // 초기 테스트 데이터
        User user1 = createUser(1L, "홍길동", new Email("hong@test.com"), new Phone("010-1234-5678"), new Money(100000L));
        User user2 = createUser(2L, "김철수", new Email("kim@test.com"), new Phone("010-2345-6789"), new Money(50000L));
        User user3 = createUser(3L, "이영희", new Email("lee@test.com"), new Phone("010-3456-7890"), new Money(200000L));

        users.put(user1.getId(), user1);
        users.put(user2.getId(), user2);
        users.put(user3.getId(), user3);

        idGenerator.set(4);
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            setId(user, idGenerator.getAndIncrement());
            setCreatedAt(user, LocalDateTime.now());
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return users.values().stream()
                .filter(user -> user.getEmail().getAddress().equals(email))
                .findFirst();
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void deleteById(Long id) {
        users.remove(id);
    }

    // Helper methods for creating test data
    private User createUser(Long id, String name, Email email, Phone phone, Money balance) {
        User user = new User(name, email, phone);
        setId(user, id);
        setBalance(user, balance);
        setCreatedAt(user, LocalDateTime.now());
        return user;
    }

    private void setId(User user, Long id) {
        try {
            java.lang.reflect.Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set id", e);
        }
    }

    private void setBalance(User user, Money balance) {
        try {
            java.lang.reflect.Field balanceField = User.class.getDeclaredField("balance");
            balanceField.setAccessible(true);
            balanceField.set(user, balance);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set balance", e);
        }
    }

    private void setCreatedAt(User user, LocalDateTime createdAt) {
        try {
            java.lang.reflect.Field createdAtField = User.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(user, createdAt);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set createdAt", e);
        }
    }

    // Test helper methods
    public void clear() {
        users.clear();
        idGenerator.set(1);
    }

    public int size() {
        return users.size();
    }
}
