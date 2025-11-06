package com.hhplus.ecommerce.domain.entity;

import com.hhplus.ecommerce.domain.vo.Email;
import com.hhplus.ecommerce.domain.vo.Money;
import com.hhplus.ecommerce.domain.vo.Phone;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    @Test
    @DisplayName("사용자 잔액을 성공적으로 충전한다")
    void chargeBalance_Success() {
        // given
        User user = new User("홍길동", new Email("hong@test.com"), new Phone("010-1234-5678"));

        // when
        user.chargeBalance(new Money(50000L));

        // then
        assertThat(user.getBalance().getAmount()).isEqualTo(50000L);
    }

    @Test
    @DisplayName("잔액을 여러 번 충전하면 누적된다")
    void chargeBalance_Multiple() {
        // given
        User user = new User("홍길동", new Email("hong@test.com"), new Phone("010-1234-5678"));

        // when
        user.chargeBalance(new Money(30000L));
        user.chargeBalance(new Money(20000L));

        // then
        assertThat(user.getBalance().getAmount()).isEqualTo(50000L);
    }

    @Test
    @DisplayName("0 이하의 금액을 충전하려고 하면 예외가 발생한다")
    void chargeBalance_InvalidAmount() {
        // given
        User user = new User("홍길동", new Email("hong@test.com"), new Phone("010-1234-5678"));

        // when & then
        assertThatThrownBy(() -> user.chargeBalance(new Money(0L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("충전 금액은 0보다 커야 합니다.");

        assertThatThrownBy(() -> user.chargeBalance(new Money(-1000L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("금액은 0 이상이어야 합니다.");
    }

    @Test
    @DisplayName("사용자 잔액을 성공적으로 차감한다")
    void deductBalance_Success() {
        // given
        User user = new User("홍길동", new Email("hong@test.com"), new Phone("010-1234-5678"));
        user.chargeBalance(new Money(100000L));

        // when
        user.deductBalance(new Money(30000L));

        // then
        assertThat(user.getBalance().getAmount()).isEqualTo(70000L);
    }

    @Test
    @DisplayName("잔액이 부족하면 차감 시 예외가 발생한다")
    void deductBalance_InsufficientBalance() {
        // given
        User user = new User("홍길동", new Email("hong@test.com"), new Phone("010-1234-5678"));
        user.chargeBalance(new Money(50000L));

        // when & then
        assertThatThrownBy(() -> user.deductBalance(new Money(60000L)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("잔액이 부족합니다.");
    }

    @Test
    @DisplayName("0 이하의 금액을 차감하려고 하면 예외가 발생한다")
    void deductBalance_InvalidAmount() {
        // given
        User user = new User("홍길동", new Email("hong@test.com"), new Phone("010-1234-5678"));
        user.chargeBalance(new Money(50000L));

        // when & then
        assertThatThrownBy(() -> user.deductBalance(new Money(0L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("차감 금액은 0보다 커야 합니다.");

        assertThatThrownBy(() -> user.deductBalance(new Money(-1000L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("금액은 0 이상이어야 합니다.");
    }
}
