package com.loopers.domain.payment;

import com.loopers.domain.payment.attribute.PaymentMethod;
import com.loopers.domain.payment.attribute.PaymentStatus;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.root;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@MockitoSettings
class PaymentServiceTest {

    @InjectMocks
    private PaymentService sut;

    @Mock
    private PaymentRepository paymentRepository;

    @DisplayName("주문 건을 결제할 때:")
    @Nested
    class Pay {

        @DisplayName("모든 값이 유효하면, 성공적으로 결제가 완료된다.")
        @Test
        void completePayment_withValidValues() {
            // given
            Long amount = Instancio.of(Long.class)
                    .generate(root(), gen -> gen.longs().range(0L, 1_000_000L))
                    .create();
            PaymentMethod method = PaymentMethod.POINT;
            Long userId = Instancio.create(Long.class);
            UUID orderId = Instancio.create(UUID.class);

            PaymentCommand.Pay command = PaymentCommand.Pay.builder()
                    .amount(amount)
                    .paymentMethod(method)
                    .userId(userId)
                    .orderId(orderId)
                    .build();

            // when
            PaymentResult.Pay result = sut.pay(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);

            verify(paymentRepository, times(1)).save(any(Payment.class));
        }

    }

}
