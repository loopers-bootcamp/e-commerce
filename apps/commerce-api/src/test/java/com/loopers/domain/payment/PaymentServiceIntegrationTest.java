package com.loopers.domain.payment;

import com.loopers.domain.payment.attribute.PaymentMethod;
import com.loopers.domain.payment.attribute.PaymentStatus;
import com.loopers.utils.DatabaseCleanUp;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.root;

@SpringBootTest
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class PaymentServiceIntegrationTest {

    private final PaymentService paymentService;

    private final EntityManager entityManager;
    private final DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("주문 건을 결제할 때:")
    @Nested
    class Pay {

        @DisplayName("모든 값이 유효하면, 결제가 완료된다.")
        @Test
        void completePayment_withValidValues() {
            // given
            Long amount = Instancio.of(Long.class)
                    .generate(root(), gen -> gen.longs().range(0L, 1_000_000L))
                    .create();
            PaymentMethod method = Instancio.create(PaymentMethod.class);
            Long userId = Instancio.create(Long.class);
            UUID orderId = Instancio.create(UUID.class);

            // when
            PaymentResult.Pay result = paymentService.pay(orderId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getPaymentId()).isNotNull();
            assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);

            Payment savedPayment = entityManager.find(Payment.class, result.getPaymentId());
            assertThat(savedPayment).isNotNull();
            assertThat(savedPayment.getId()).isEqualTo(result.getPaymentId());
            assertThat(savedPayment.getAmount()).isEqualTo(amount);
            assertThat(savedPayment.getStatus()).isEqualTo(result.getPaymentStatus());
            assertThat(savedPayment.getMethod()).isEqualTo(method);
            assertThat(savedPayment.getUserId()).isEqualTo(userId);
            assertThat(savedPayment.getOrderId()).isEqualTo(orderId);
        }

    }

}
