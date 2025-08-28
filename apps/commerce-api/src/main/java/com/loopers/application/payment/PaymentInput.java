package com.loopers.application.payment;

import com.loopers.domain.payment.attribute.CardNumber;
import com.loopers.domain.payment.attribute.CardType;
import com.loopers.domain.payment.attribute.PaymentMethod;
import lombok.*;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PaymentInput {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Ready {
        private final String userName;
        private final UUID orderId;
        private final PaymentMethod paymentMethod;
        @Nullable
        private final CardType cardType;
        @Nullable
        private final CardNumber cardNumber;
    }

    // -------------------------------------------------------------------------------------------------

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Conclude {
        private final String transactionKey;
        private final UUID orderId;
        private final Long amount;
        private final String status;
        @Nullable
        private final String reason;
    }

}
