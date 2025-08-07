package com.loopers.interfaces.api.payment;

import com.loopers.application.payment.PaymentOutput;
import com.loopers.domain.payment.PaymentResult;
import com.loopers.domain.payment.attribute.PaymentStatus;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PaymentResponse {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Pay {
        private final Long paymentId;
        private final PaymentStatus paymentStatus;

        public static Pay from(PaymentOutput.Pay output) {
            return Pay.builder()
                    .paymentId(output.getPaymentId())
                    .paymentStatus(output.getPaymentStatus())
                    .build();
        }
    }

}
