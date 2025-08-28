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
                    "order:%s".formatted(payment.getOrderId()),
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

    public record Success(
            String transactionKey,
            UUID orderId,
            Long paymentId
    ) {
    }

    // -------------------------------------------------------------------------------------------------

    public record Failed(
            String transactionKey,
            String reason,
            UUID orderId,
            Long paymentId
    ) {
    }

}
