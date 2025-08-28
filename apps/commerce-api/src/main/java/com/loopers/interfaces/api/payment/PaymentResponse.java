package com.loopers.interfaces.api.payment;

import com.loopers.application.payment.PaymentOutput;
import com.loopers.domain.payment.attribute.PaymentStatus;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PaymentResponse {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Ready {
        private final Long paymentId;
        private final PaymentStatus paymentStatus;

        public static Ready from(PaymentOutput.Ready output) {
            return Ready.builder()
                    .paymentId(output.getPaymentId())
                    .paymentStatus(output.getPaymentStatus())
                    .build();
        }
    }

}
