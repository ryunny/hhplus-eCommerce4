package com.hhplus.ecommerce.application.usecase;

import com.hhplus.ecommerce.domain.entity.User;
import com.hhplus.ecommerce.domain.repository.UserRepository;
import com.hhplus.ecommerce.domain.vo.Money;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class UserBalanceUseCase {

    private final UserRepository userRepository;

    // 사용자별 락 객체를 관리하는 Map (잔액 동시성 제어)
    private final ConcurrentHashMap<Long, ReentrantLock> userLocks = new ConcurrentHashMap<>();

    private static final long LOCK_TIMEOUT_SECONDS = 5;

    public UserBalanceUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User chargeBalance(Long userId, Long amount) {
        // 사용자별 락 획득 (공정성 보장)
        ReentrantLock lock = userLocks.computeIfAbsent(userId, k -> new ReentrantLock(true));

        try {
            // 타임아웃과 함께 락 획득 시도
            if (!lock.tryLock(LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                throw new IllegalStateException("잔액 충전 요청이 혼잡합니다. 다시 시도해주세요.");
            }

            try {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

                Money chargeAmount = new Money(amount);
                user.chargeBalance(chargeAmount);
                return userRepository.save(user);
            } finally {
                lock.unlock();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("잔액 충전이 중단되었습니다.", e);
        }
    }

    // 단순 조회는 동시성 제어 불필요
    public User getBalance(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
    }
}
