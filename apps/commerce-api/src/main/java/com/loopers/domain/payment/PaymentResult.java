package com.loopers.domain.payment;

import com.loopers.domain.payment.attribute.PaymentMethod;
import com.loopers.domain.payment.attribute.PaymentStatus;
import lombok.*;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PaymentResult {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class GetPayment {
        private final Long paymentId;
        private final Long amount;
        private final PaymentStatus paymentStatus;
        private final PaymentMethod paymentMethod;
        private final Long userId;
        private final UUID orderId;

        public static GetPayment from(Payment payment) {
            return GetPayment.builder()
                    .paymentId(payment.getId())
                    .amount(payment.getAmount())
                    .paymentStatus(payment.getStatus())
                    .paymentMethod(payment.getMethod())
                    .userId(payment.getUserId())
                    .orderId(payment.getOrderId())
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

        public static Pay from(Payment payment) {
            return Pay.builder()
                    .paymentId(payment.getId())
                    .paymentStatus(payment.getStatus())
                    .build();
        }
    }

}
