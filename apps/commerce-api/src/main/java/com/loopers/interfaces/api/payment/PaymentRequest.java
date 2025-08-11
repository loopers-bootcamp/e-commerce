package com.loopers.interfaces.api.payment;

import com.loopers.domain.payment.attribute.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PaymentRequest {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Pay {
        @NotNull
        private final UUID orderId;
        @NotNull
        private final PaymentMethod paymentMethod;
    }

}
