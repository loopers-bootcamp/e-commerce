package com.loopers.domain.payment;

import com.loopers.domain.payment.attribute.PaymentStatus;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PaymentResult {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Pay {
        private final Long paymentId;
        private final PaymentStatus paymentStatus;

        public static Pay from(Payment payment) {
            return Pay.builder()
                    .paymentId(payment.getId())
                    .paymentStatus(payment.getStatus())
                    .build();
        }
    }

}
