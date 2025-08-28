package com.loopers.application.payment;

import com.loopers.domain.payment.PaymentResult;
import com.loopers.domain.payment.attribute.PaymentStatus;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PaymentOutput {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Ready {
        private final Long paymentId;
        private final PaymentStatus paymentStatus;

        public static Ready from(PaymentResult.Ready result) {
            return builder()
                    .paymentId(result.getPaymentId())
                    .paymentStatus(result.getPaymentStatus())
                    .build();
        }
    }

    // -------------------------------------------------------------------------------------------------

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Pay {
        private final Long paymentId;
        private final PaymentStatus paymentStatus;

        public static Pay from(PaymentResult.Pending result) {
            return builder()
                    .paymentId(result.getPaymentId())
                    .paymentStatus(result.getPaymentStatus())
                    .build();
        }

        public static Pay from(PaymentResult.Pay result) {
            return builder()
                    .paymentId(result.getPaymentId())
                    .paymentStatus(result.getPaymentStatus())
                    .build();
        }
    }

}
