package com.hhplus.ecommerce.application.usecase.user;

import com.hhplus.ecommerce.BaseIntegrationTest;
import com.hhplus.ecommerce.application.command.ChargeBalanceCommand;
import com.hhplus.ecommerce.domain.entity.User;
import com.hhplus.ecommerce.domain.repository.UserRepository;
import com.hhplus.ecommerce.domain.vo.Email;
import com.hhplus.ecommerce.domain.vo.Phone;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.*;

/**
 * ChargeBalanceUseCase 통합 테스트
 * UseCase 레이어의 전체 플로우를 테스트
 */
class ChargeBalanceUseCaseIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ChargeBalanceUseCase chargeBalanceUseCase;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("잔액 충전 UseCase가 정상 동작한다")
    void executeChargeBalance() {
        // given
        User user = new User("테스트유저", new Email("test@example.com"), new Phone("010-0000-0000"));
        User savedUser = userRepository.save(user);
        String publicId = savedUser.getPublicId();

        ChargeBalanceCommand command = new ChargeBalanceCommand(publicId, 100000L);

        // when
        User result = chargeBalanceUseCase.execute(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getBalance().getAmount()).isEqualTo(100000L);
        assertThat(result.getPublicId()).isEqualTo(publicId);
    }

    @Test
    @DisplayName("여러 번 충전하면 잔액이 누적된다")
    void executeMultipleCharges() {
        // given
        User user = new User("누적테스트", new Email("accumulate@example.com"), new Phone("010-1111-1111"));
        User savedUser = userRepository.save(user);
        String publicId = savedUser.getPublicId();

        // when
        chargeBalanceUseCase.execute(new ChargeBalanceCommand(publicId, 10000L));
        chargeBalanceUseCase.execute(new ChargeBalanceCommand(publicId, 20000L));
        User result = chargeBalanceUseCase.execute(new ChargeBalanceCommand(publicId, 30000L));

        // then
        assertThat(result.getBalance().getAmount()).isEqualTo(60000L);
    }

    @Test
    @DisplayName("0원 이하 금액은 충전할 수 없다")
    void chargeZeroOrNegativeAmount() {
        // given
        User user = new User("실패테스트", new Email("fail@example.com"), new Phone("010-2222-2222"));
        User savedUser = userRepository.save(user);
        String publicId = savedUser.getPublicId();

        ChargeBalanceCommand zeroCommand = new ChargeBalanceCommand(publicId, 0L);
        ChargeBalanceCommand negativeCommand = new ChargeBalanceCommand(publicId, -1000L);

        // when & then
        assertThatThrownBy(() -> chargeBalanceUseCase.execute(zeroCommand))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("충전 금액은 0보다 커야 합니다");

        assertThatThrownBy(() -> chargeBalanceUseCase.execute(negativeCommand))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("충전 금액은 0보다 커야 합니다");
    }

    @Test
    @DisplayName("존재하지 않는 publicId로 충전하면 예외가 발생한다")
    void chargeWithInvalidPublicId() {
        // given
        ChargeBalanceCommand command = new ChargeBalanceCommand("invalid-uuid", 10000L);

        // when & then
        assertThatThrownBy(() -> chargeBalanceUseCase.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");
    }
}
