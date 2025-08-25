package com.loopers.domain.payment.event;

import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.attribute.CardNumber;
import com.loopers.domain.payment.attribute.CardType;

import java.util.UUID;

public record PaymentEvent() {

    public record Ready(
            Long paymentId,
            UUID orderId,
            CardType cardType,
            CardNumber cardNumber,
            Long amount
    ) {
        public static Ready from(Payment payment) {
            return new Ready(
                    payment.getId(),
                    payment.getOrderId(),
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
