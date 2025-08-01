package com.loopers.domain.payment;

import com.loopers.domain.payment.attribute.PaymentMethod;
import lombok.*;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PaymentCommand {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Pay {
        private final Long amount;
        private final PaymentMethod paymentMethod;
        private final Long userId;
        private final UUID orderId;
    }

}
