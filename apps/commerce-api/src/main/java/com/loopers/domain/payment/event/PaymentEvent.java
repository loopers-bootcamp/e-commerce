package com.loopers.domain.payment.event;

import com.loopers.domain.common.DomainEvent;
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
            Long amount,
            Long userId
    ) implements SagaEvent, DomainEvent {
        public static Ready from(Payment payment) {
            return new Ready(
                    payment.getId().toString(),
                    "payment.ready",
                    payment.getId(),
                    payment.getOrderId(),
                    payment.getMethod(),
                    payment.getCardType(),
                    payment.getCardNumber(),
                    payment.getAmount(),
                    payment.getUserId()
            );
        }
    }

    // -------------------------------------------------------------------------------------------------

    public record Paid(
            String eventKey,
            String eventName,
            Long paymentId,
            UUID orderId,
            Long userId
    ) implements SagaEvent, DomainEvent {
        public static Paid from(Payment payment) {
            return new Paid(
                    payment.getId().toString(),
                    "payment.paid",
                    payment.getId(),
                    payment.getOrderId(),
                    payment.getUserId()
            );
        }
    }

    // -------------------------------------------------------------------------------------------------

    public record Failed(
            String eventKey,
            String eventName,
            Long paymentId,
            UUID orderId,
            Long userId
    ) implements SagaEvent, DomainEvent {
        public static Failed from(Payment payment) {
            return new Failed(
                    payment.getId().toString(),
                    "payment.failed",
                    payment.getId(),
                    payment.getOrderId(),
                    payment.getUserId()
            );
        }
    }

}
