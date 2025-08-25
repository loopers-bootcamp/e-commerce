package com.loopers.interfaces.callback.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PaymentCallbackRequest {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ProcessTransaction {
        @NotBlank
        private final String transactionKey;
        @NotNull
        private final UUID orderId;
        @NotBlank
        private final String cardType;
        @NotBlank
        private final String cardNo;
        @NotNull
        @PositiveOrZero
        private final Long amount;
        @NotBlank
        private final String status;
        @Nullable
        private final String reason;
    }

}
