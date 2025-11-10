package com.hhplus.ecommerce.domain.service;

import com.hhplus.ecommerce.BaseIntegrationTest;
import com.hhplus.ecommerce.domain.entity.User;
import com.hhplus.ecommerce.domain.repository.UserRepository;
import com.hhplus.ecommerce.domain.vo.Email;
import com.hhplus.ecommerce.domain.vo.Money;
import com.hhplus.ecommerce.domain.vo.Phone;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.*;

/**
 * UserService 통합 테스트
 * Testcontainers를 사용한 실제 DB 환경 테스트
 */
class UserServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("사용자를 생성하고 조회할 수 있다")
    void createAndGetUser() {
        // given
        User user = new User("홍길동", new Email("hong@test.com"), new Phone("010-1234-5678"));
        User savedUser = userRepository.save(user);

        // when
        User foundUser = userService.getUser(savedUser.getId());

        // then
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getName()).isEqualTo("홍길동");
        assertThat(foundUser.getEmail().getAddress()).isEqualTo("hong@test.com");
        assertThat(foundUser.getPublicId()).isNotNull(); // UUID 자동 생성 확인
    }

    @Test
    @DisplayName("publicId로 사용자를 조회할 수 있다")
    void getUserByPublicId() {
        // given
        User user = new User("김철수", new Email("kim@test.com"), new Phone("010-9876-5432"));
        User savedUser = userRepository.save(user);
        String publicId = savedUser.getPublicId();

        // when
        User foundUser = userService.getUserByPublicId(publicId);

        // then
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getName()).isEqualTo("김철수");
        assertThat(foundUser.getPublicId()).isEqualTo(publicId);
    }

    @Test
    @DisplayName("잔액을 충전할 수 있다")
    void chargeBalance() {
        // given
        User user = new User("이영희", new Email("lee@test.com"), new Phone("010-1111-2222"));
        User savedUser = userRepository.save(user);
        Money chargeAmount = new Money(10000L);

        // when
        User chargedUser = userService.chargeBalance(savedUser.getId(), chargeAmount);

        // then
        assertThat(chargedUser.getBalance().getAmount()).isEqualTo(10000L);
    }

    @Test
    @DisplayName("publicId로 잔액을 충전할 수 있다")
    void chargeBalanceByPublicId() {
        // given
        User user = new User("박민수", new Email("park@test.com"), new Phone("010-3333-4444"));
        User savedUser = userRepository.save(user);
        String publicId = savedUser.getPublicId();
        Money chargeAmount = new Money(50000L);

        // when
        User chargedUser = userService.chargeBalanceByPublicId(publicId, chargeAmount);

        // then
        assertThat(chargedUser.getBalance().getAmount()).isEqualTo(50000L);
    }

    @Test
    @DisplayName("잔액을 차감할 수 있다")
    void deductBalance() {
        // given
        User user = new User("최지은", new Email("choi@test.com"), new Phone("010-5555-6666"));
        User savedUser = userRepository.save(user);
        userService.chargeBalance(savedUser.getId(), new Money(20000L));

        // when
        userService.deductBalance(savedUser.getId(), new Money(5000L));

        // then
        User updatedUser = userService.getUser(savedUser.getId());
        assertThat(updatedUser.getBalance().getAmount()).isEqualTo(15000L);
    }

    @Test
    @DisplayName("잔액이 부족하면 차감할 수 없다")
    void deductBalanceWithInsufficientBalance() {
        // given
        User user = new User("정수진", new Email("jung@test.com"), new Phone("010-7777-8888"));
        User savedUser = userRepository.save(user);
        userService.chargeBalance(savedUser.getId(), new Money(10000L));

        // when & then
        assertThatThrownBy(() ->
                userService.deductBalance(savedUser.getId(), new Money(20000L))
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("잔액이 부족합니다");
    }

    @Test
    @DisplayName("비관적 락을 사용하여 잔액을 조회할 수 있다")
    void findByIdWithLock() {
        // given
        User user = new User("강민지", new Email("kang@test.com"), new Phone("010-9999-0000"));
        User savedUser = userRepository.save(user);

        // when
        User lockedUser = userRepository.findByIdWithLock(savedUser.getId()).orElseThrow();

        // then
        assertThat(lockedUser).isNotNull();
        assertThat(lockedUser.getName()).isEqualTo("강민지");
    }

    @Test
    @DisplayName("존재하지 않는 사용자를 조회하면 예외가 발생한다")
    void getUserNotFound() {
        // when & then
        assertThatThrownBy(() -> userService.getUser(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("잔액 검증이 정상 동작한다")
    void validateBalance() {
        // given
        User user = new User("윤서연", new Email("yoon@test.com"), new Phone("010-1234-9999"));
        User savedUser = userRepository.save(user);
        userService.chargeBalance(savedUser.getId(), new Money(30000L));

        User userWithBalance = userService.getUser(savedUser.getId());
        Money requiredAmount = new Money(20000L);

        // when & then - 잔액이 충분한 경우
        assertThatCode(() -> userService.validateBalance(userWithBalance, requiredAmount))
                .doesNotThrowAnyException();

        // when & then - 잔액이 부족한 경우
        Money insufficientAmount = new Money(50000L);
        assertThatThrownBy(() ->
                userService.validateBalance(userWithBalance, insufficientAmount)
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("잔액이 부족합니다");
    }
}
