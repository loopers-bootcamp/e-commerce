package com.loopers.domain.payment;

import com.loopers.domain.payment.attribute.CardNumber;
import com.loopers.domain.payment.attribute.CardType;
import com.loopers.domain.payment.attribute.PaymentMethod;
import lombok.*;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PaymentCommand {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class GetPayment {
        private final UUID orderId;
        private final Long userId;
    }

    // -------------------------------------------------------------------------------------------------

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Ready {
        private final Long amount;
        private final PaymentMethod paymentMethod;
        private final CardType cardType;
        private final CardNumber cardNumber;
        private final Long userId;
        private final UUID orderId;
    }

    // -------------------------------------------------------------------------------------------------

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Pay {
        private final Long amount;
        private final PaymentMethod paymentMethod;
        private final Long userId;
        private final UUID orderId;
    }

    // PaymentAttempt ----------------------------------------------------------------------------------

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class RecordResponse {
        private final UUID merchantUid;
        private final String transactionKey;
        private final Long paymentId;
    }

}
