package com.loopers.infrastructure.payment.client;

import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.attribute.CardType;

import java.util.UUID;

public class PgSimulatorRequest {

    public record RequestTransaction(
            UUID orderId,
            CardType cardType,
            String cardNo,
            Long amount,
            String callbackUrl
    ) {
        public static RequestTransaction of(Payment payment, String callbackUrl) {
            return new RequestTransaction(
                    payment.getOrderId(),
                    payment.getCardType(),
                    payment.getCardNo(),
                    payment.getAmount(),
                    callbackUrl
            );
        }
    }

}
