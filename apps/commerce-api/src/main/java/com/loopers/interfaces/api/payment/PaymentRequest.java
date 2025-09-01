package com.loopers.interfaces.api.payment;

import com.loopers.domain.payment.attribute.CardNumber;
import com.loopers.domain.payment.attribute.CardType;
import com.loopers.domain.payment.attribute.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PaymentRequest {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Ready {
        @NotNull
        private final UUID orderId;
        @NotNull
        private final PaymentMethod paymentMethod;
        @Nullable
        private final CardType cardType;
        @Nullable
        private final CardNumber cardNumber;
    }

}
