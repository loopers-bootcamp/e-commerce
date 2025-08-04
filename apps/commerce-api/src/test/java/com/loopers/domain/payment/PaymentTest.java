package com.loopers.domain.payment;

import com.loopers.domain.payment.attribute.PaymentMethod;
import com.loopers.domain.payment.attribute.PaymentStatus;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
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

        @DisplayName("모든 값이 유효하면, 성공적으로 생성된다.")
        @Test
        void createPayment_withValidValues() {
            // given
            Long amount = Instancio.of(Long.class)
                    .generate(root(), gen -> gen.longs().range(0L, 1_000_000L))
                    .create();
            PaymentStatus status = Instancio.create(PaymentStatus.class);
            PaymentMethod method = Instancio.create(PaymentMethod.class);
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
            assertThat(payment.getUserId()).isEqualTo(userId);
            assertThat(payment.getOrderId()).isEqualTo(orderId);
        }

    }

}
