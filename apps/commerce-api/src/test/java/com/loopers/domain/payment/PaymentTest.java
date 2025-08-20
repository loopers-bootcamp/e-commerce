package com.loopers.domain.payment;

import com.loopers.domain.payment.attribute.CardNumber;
import com.loopers.domain.payment.attribute.CardType;
import com.loopers.domain.payment.attribute.PaymentMethod;
import com.loopers.domain.payment.attribute.PaymentStatus;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.instancio.Select.root;

class PaymentTest {

    @DisplayName("결제를 생성할 때:")
    @Nested
    class Create {

        @DisplayName("결제 금액이 유효하지 않으면, BusinessException이 발생한다.")
        @NullSource
        @ValueSource(longs = {
                Long.MIN_VALUE, -100, -1,
        })
        @ParameterizedTest
        void throwException_withInvalidAmount(Long amount) {
            // given
            PaymentStatus status = Instancio.create(PaymentStatus.class);
            PaymentMethod method = Instancio.create(PaymentMethod.class);
            Long userId = Instancio.create(Long.class);
            UUID orderId = Instancio.create(UUID.class);

            // when & then
            assertThatException()
                    .isThrownBy(() -> Payment.builder()
                            .amount(amount)
                            .status(status)
                            .method(method)
                            .userId(userId)
                            .orderId(orderId)
                            .build())
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorType", CommonErrorType.INVALID);
        }

        @DisplayName("결제 상태가 유효하지 않으면, BusinessException이 발생한다.")
        @NullSource
        @ParameterizedTest
        void throwException_withInvalidStatus(PaymentStatus status) {
            // given
            Long amount = Instancio.of(Long.class)
                    .generate(root(), gen -> gen.longs().range(0L, 1_000_000L))
                    .create();
            PaymentMethod method = Instancio.create(PaymentMethod.class);
            Long userId = Instancio.create(Long.class);
            UUID orderId = Instancio.create(UUID.class);

            // when & then
            assertThatException()
                    .isThrownBy(() -> Payment.builder()
                            .amount(amount)
                            .status(status)
                            .method(method)
                            .userId(userId)
                            .orderId(orderId)
                            .build())
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorType", CommonErrorType.INVALID);
        }

        @DisplayName("결제 수단이 유효하지 않으면, BusinessException이 발생한다.")
        @NullSource
        @ParameterizedTest
        void throwException_withInvalidMethod(PaymentMethod method) {
            // given
            Long amount = Instancio.of(Long.class)
                    .generate(root(), gen -> gen.longs().range(0L, 1_000_000L))
                    .create();
            PaymentStatus status = Instancio.create(PaymentStatus.class);
            Long userId = Instancio.create(Long.class);
            UUID orderId = Instancio.create(UUID.class);

            // when & then
            assertThatException()
                    .isThrownBy(() -> Payment.builder()
                            .amount(amount)
                            .status(status)
                            .method(method)
                            .userId(userId)
                            .orderId(orderId)
                            .build())
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorType", CommonErrorType.INVALID);
        }

        @DisplayName("사용자 아이디가 유효하지 않으면, BusinessException이 발생한다.")
        @NullSource
        @ParameterizedTest
        void throwException_withInvalidUserId(Long userId) {
            // given
            Long amount = Instancio.of(Long.class)
                    .generate(root(), gen -> gen.longs().range(0L, 1_000_000L))
                    .create();
            PaymentStatus status = Instancio.create(PaymentStatus.class);
            PaymentMethod method = Instancio.create(PaymentMethod.class);
            UUID orderId = Instancio.create(UUID.class);

            // when & then
            assertThatException()
                    .isThrownBy(() -> Payment.builder()
                            .amount(amount)
                            .status(status)
                            .method(method)
                            .userId(userId)
                            .orderId(orderId)
                            .build())
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorType", CommonErrorType.INVALID);
        }

        @DisplayName("주문 아이디가 유효하지 않으면, BusinessException이 발생한다.")
        @NullSource
        @ParameterizedTest
        void throwException_withInvalidOrderId(UUID orderId) {
            // given
            Long amount = Instancio.of(Long.class)
                    .generate(root(), gen -> gen.longs().range(0L, 1_000_000L))
                    .create();
            PaymentStatus status = Instancio.create(PaymentStatus.class);
            PaymentMethod method = Instancio.create(PaymentMethod.class);
            Long userId = Instancio.create(Long.class);

            // when & then
            assertThatException()
                    .isThrownBy(() -> Payment.builder()
                            .amount(amount)
                            .status(status)
                            .method(method)
                            .userId(userId)
                            .orderId(orderId)
                            .build())
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorType", CommonErrorType.INVALID);
        }

        @DisplayName("포인트 결제를 성공적으로 생성한다.")
        @EnumSource(value = PaymentMethod.class, names = "POINT")
        @ParameterizedTest
        void createPayment_withPointMethod(PaymentMethod method) {
            // given
            Long amount = Instancio.of(Long.class)
                    .generate(root(), gen -> gen.longs().range(0L, 1_000_000L))
                    .create();
            PaymentStatus status = Instancio.create(PaymentStatus.class);
            Long userId = Instancio.create(Long.class);
            UUID orderId = Instancio.create(UUID.class);

            // when
            Payment payment = Payment.builder()
                    .amount(amount)
                    .status(status)
                    .method(method)
                    .userId(userId)
                    .orderId(orderId)
                    .build();

            // then
            assertThat(payment).isNotNull();
            assertThat(payment.getAmount()).isEqualTo(amount);
            assertThat(payment.getStatus()).isEqualTo(status);
            assertThat(payment.getMethod()).isEqualTo(method);
            assertThat(payment.getCardType()).isNull();
            assertThat(payment.getCardNumber()).isNull();
            assertThat(payment.getUserId()).isEqualTo(userId);
            assertThat(payment.getOrderId()).isEqualTo(orderId);
        }

        @DisplayName("카드 결제를 성공적으로 생성한다.")
        @EnumSource(value = PaymentMethod.class, names = "CARD")
        @ParameterizedTest
        void createPayment_withCardMethod(PaymentMethod method) {
            // given
            Long amount = Instancio.of(Long.class)
                    .generate(root(), gen -> gen.longs().range(0L, 1_000_000L))
                    .create();
            PaymentStatus status = Instancio.create(PaymentStatus.class);
            Long userId = Instancio.create(Long.class);
            UUID orderId = Instancio.create(UUID.class);

            CardType cardType = Instancio.create(CardType.class);
            CardNumber cardNumber = Instancio.of(String.class)
                    .generate(root(), gen -> gen.string().digits().length(16))
                    .stream().map(CardNumber::new)
                    .limit(1)
                    .findAny()
                    .orElseThrow();

            // when
            Payment payment = Payment.builder()
                    .amount(amount)
                    .status(status)
                    .method(method)
                    .cardType(cardType)
                    .cardNumber(cardNumber)
                    .userId(userId)
                    .orderId(orderId)
                    .build();

            // then
            assertThat(payment).isNotNull();
            assertThat(payment.getAmount()).isEqualTo(amount);
            assertThat(payment.getStatus()).isEqualTo(status);
            assertThat(payment.getMethod()).isEqualTo(method);
            assertThat(payment.getCardType()).isEqualTo(cardType);
            assertThat(payment.getCardNumber()).isEqualTo(cardNumber);
            assertThat(payment.getUserId()).isEqualTo(userId);
            assertThat(payment.getOrderId()).isEqualTo(orderId);
        }

    }

}
