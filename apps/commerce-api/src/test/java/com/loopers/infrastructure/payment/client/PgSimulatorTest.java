package com.loopers.infrastructure.payment.client;

import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.attribute.CardType;
import com.loopers.domain.payment.attribute.PaymentMethod;
import com.loopers.domain.payment.attribute.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PgSimulatorTest {

    @Autowired
    private PgSimulator pgSimulator;

    @Test
    void requestTransaction() {
        Payment payment = Payment.builder()
                .amount(15000L)
                .status(PaymentStatus.READY)
                .method(PaymentMethod.CARD)
                .cardType(CardType.HYUNDAI)
                .cardNo("1234-5678-9012-3456")
                .userId(1L)
                .orderId(UUID.randomUUID())
                .build();
        pgSimulator.requestTransaction(payment);
    }

}
