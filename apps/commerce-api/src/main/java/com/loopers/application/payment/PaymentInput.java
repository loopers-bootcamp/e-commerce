package com.loopers.application.payment;

import com.loopers.domain.payment.attribute.CardNumber;
import com.loopers.domain.payment.attribute.CardType;
import com.loopers.domain.payment.attribute.PaymentMethod;
import lombok.*;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PaymentInput {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Pay {
        private final String userName;
        private final UUID orderId;
        private final PaymentMethod paymentMethod;
        private final CardType cardType;
        private final CardNumber cardNumber;
    }

}
