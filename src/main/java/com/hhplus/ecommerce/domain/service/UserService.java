package com.hhplus.ecommerce.domain.service;

import com.hhplus.ecommerce.domain.entity.User;
import com.hhplus.ecommerce.domain.repository.UserRepository;
import com.hhplus.ecommerce.domain.vo.Money;
import org.springframework.stereotype.Service;

/**
 * 사용자 관련 비즈니스 로직을 처리하는 서비스
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 사용자 조회
     *
     * @param userId 사용자 ID
     * @return 사용자 엔티티
     */
    public User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
    }

    /**
     * 잔액 충분성 검증
     *
     * @param user 사용자
     * @param amount 필요 금액
     * @throws IllegalStateException 잔액이 부족한 경우
     */
    public void validateBalance(User user, Money amount) {
        if (!user.hasEnoughBalance(amount)) {
            throw new IllegalStateException("잔액이 부족합니다.");
        }
    }

    /**
     * 잔액 차감
     * 비즈니스 로직(잔액 차감)과 영속화를 함께 처리
     *
     * DB 전환 시: Repository의 findById에 @Lock(LockModeType.PESSIMISTIC_WRITE) 적용 필요
     *
     * @param userId 사용자 ID
     * @param amount 차감할 금액
     */
    public void deductBalance(Long userId, Money amount) {
        User user = getUser(userId);
        user.deductBalance(amount);
        userRepository.save(user);
    }

    /**
     * 잔액 충전
     *
     * @param userId 사용자 ID
     * @param amount 충전할 금액
     */
    public void chargeBalance(Long userId, Money amount) {
        User user = getUser(userId);
        user.chargeBalance(amount);
        userRepository.save(user);
    }
}
