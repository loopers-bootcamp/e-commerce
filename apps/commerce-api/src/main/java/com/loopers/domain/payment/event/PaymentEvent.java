package com.loopers.domain.payment.event;

import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.attribute.CardNumber;
import com.loopers.domain.payment.attribute.CardType;
import com.loopers.domain.payment.attribute.PaymentMethod;
import com.loopers.domain.saga.event.SagaEvent;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public record PaymentEvent() {

    public record Ready(
            String eventKey,
            String eventName,
            Long paymentId,
            UUID orderId,
            PaymentMethod paymentMethod,
            @Nullable CardType cardType,
            @Nullable CardNumber cardNumber,
            Long amount
    ) implements SagaEvent {
        public static Ready from(Payment payment) {
            return new Ready(
                    "payment:%d".formatted(payment.getId()),
                    "payment.ready",
                    payment.getId(),
                    payment.getOrderId(),
                    payment.getMethod(),
                    payment.getCardType(),
                    payment.getCardNumber(),
                    payment.getAmount()
            );
        }
    }

    // -------------------------------------------------------------------------------------------------

    public record Paid(
            String eventKey,
            String eventName,
            Long paymentId,
            UUID orderId
    ) implements SagaEvent {
        public static Paid from(Payment payment) {
            return new Paid(
                    "payment:%d".formatted(payment.getId()),
                    "payment.paid",
                    payment.getId(),
                    payment.getOrderId()
            );
        }
    }

    // -------------------------------------------------------------------------------------------------

    public record Failed(
            String eventKey,
            String eventName,
            Long paymentId,
            UUID orderId
    ) implements SagaEvent {
        public static Failed from(Payment payment) {
            return new Failed(
                    "payment:%d".formatted(payment.getId()),
                    "payment.failed",
                    payment.getId(),
                    payment.getOrderId()
            );
        }
    }

}
