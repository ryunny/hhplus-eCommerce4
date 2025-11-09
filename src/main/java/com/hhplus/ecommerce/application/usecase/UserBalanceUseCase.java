package com.hhplus.ecommerce.application.usecase;

import com.hhplus.ecommerce.domain.entity.User;
import com.hhplus.ecommerce.domain.repository.UserRepository;
import com.hhplus.ecommerce.domain.vo.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserBalanceUseCase {

    private final UserRepository userRepository;

    public UserBalanceUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 잔액 충전
     * 비관적 락과 더티 체킹을 활용하여 동시성 제어
     */
    @Transactional
    public User chargeBalance(Long userId, Long amount) {
        User user = userRepository.findByIdWithLock(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        Money chargeAmount = new Money(amount);
        user.chargeBalance(chargeAmount);
        // 더티 체킹으로 자동 저장 (save() 불필요)

        return user;
    }

    // 단순 조회는 동시성 제어 불필요
    public User getBalance(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
    }
}
