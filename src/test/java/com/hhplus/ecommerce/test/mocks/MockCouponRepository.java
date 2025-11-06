package com.hhplus.ecommerce.test.mocks;

import com.hhplus.ecommerce.domain.entity.Coupon;
import com.hhplus.ecommerce.domain.repository.CouponRepository;
import com.hhplus.ecommerce.domain.vo.DiscountRate;
import com.hhplus.ecommerce.domain.vo.Money;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class MockCouponRepository implements CouponRepository {

    private final Map<Long, Coupon> coupons = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public MockCouponRepository() {
        // 초기 테스트 데이터
        LocalDateTime now = LocalDateTime.now();

        Coupon percentCoupon = createCoupon(
                1L, "10% 할인 쿠폰", "PERCENTAGE", new DiscountRate(10), null,
                new Money(50000L), 100, 0, now.minusDays(1), now.plusDays(30)
        );

        Coupon amountCoupon = createCoupon(
                2L, "5000원 할인 쿠폰", "AMOUNT", null, new Money(5000L),
                new Money(30000L), 200, 0, now.minusDays(1), now.plusDays(30)
        );

        Coupon expiredCoupon = createCoupon(
                3L, "만료된 쿠폰", "PERCENTAGE", new DiscountRate(20), null,
                new Money(100000L), 50, 0, now.minusDays(10), now.minusDays(1)
        );

        coupons.put(percentCoupon.getId(), percentCoupon);
        coupons.put(amountCoupon.getId(), amountCoupon);
        coupons.put(expiredCoupon.getId(), expiredCoupon);

        idGenerator.set(4);
    }

    @Override
    public Coupon save(Coupon coupon) {
        if (coupon.getId() == null) {
            setId(coupon, idGenerator.getAndIncrement());
        }
        coupons.put(coupon.getId(), coupon);
        return coupon;
    }

    @Override
    public Optional<Coupon> findById(Long id) {
        return Optional.ofNullable(coupons.get(id));
    }

    @Override
    public List<Coupon> findAll() {
        return new ArrayList<>(coupons.values());
    }

    @Override
    public List<Coupon> findIssuableCoupons() {
        return coupons.values().stream()
                .filter(Coupon::isIssuable)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        coupons.remove(id);
    }

    // Helper methods for creating test data
    private Coupon createCoupon(Long id, String name, String couponType,
                                DiscountRate discountRate, Money discountAmount,
                                Money minOrderAmount, Integer totalQuantity,
                                Integer issuedQuantity, LocalDateTime startDate,
                                LocalDateTime endDate) {
        Coupon coupon = new Coupon(name, couponType, discountRate,
                discountAmount, minOrderAmount, totalQuantity, startDate, endDate);
        setId(coupon, id);
        setIssuedQuantity(coupon, issuedQuantity);
        return coupon;
    }

    private void setId(Coupon coupon, Long id) {
        try {
            java.lang.reflect.Field idField = Coupon.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(coupon, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set id", e);
        }
    }

    private void setIssuedQuantity(Coupon coupon, Integer issuedQuantity) {
        try {
            java.lang.reflect.Field issuedQuantityField = Coupon.class.getDeclaredField("issuedQuantity");
            issuedQuantityField.setAccessible(true);
            issuedQuantityField.set(coupon, issuedQuantity);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set issuedQuantity", e);
        }
    }

    // Test helper methods
    public void clear() {
        coupons.clear();
        idGenerator.set(1);
    }

    public int size() {
        return coupons.size();
    }
}
