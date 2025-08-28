package com.loopers.domain.payment;

import com.loopers.domain.payment.attribute.CardNumber;
import com.loopers.domain.payment.attribute.CardType;
import com.loopers.domain.payment.attribute.PaymentMethod;
import com.loopers.domain.payment.attribute.PaymentStatus;
import lombok.*;
import org.jspecify.annotations.Nullable;

import java.util.List;
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
        @Nullable
        private final CardType cardType;
        @Nullable
        private final CardNumber cardNumber;
        private final Long userId;
        private final UUID orderId;

        public static GetPayment from(Payment payment) {
            return builder()
                    .paymentId(payment.getId())
                    .amount(payment.getAmount())
                    .paymentStatus(payment.getStatus())
                    .paymentMethod(payment.getMethod())
                    .cardType(payment.getCardType())
                    .cardNumber(payment.getCardNumber())
                    .userId(payment.getUserId())
                    .orderId(payment.getOrderId())
                    .build();
        }
    }

    // -------------------------------------------------------------------------------------------------

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class GetInconclusivePayments {
        private final List<Item> items;

        public static GetInconclusivePayments from(List<Payment> payments) {
            return builder()
                    .items(payments.stream()
                            .map(payment -> new Item(
                                    payment.getId(),
                                    payment.getAmount(),
                                    payment.getUserId(),
                                    payment.getOrderId()
                            ))
                            .toList()
                    )
                    .build();
        }

        @Getter
        @Builder
        @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Item {
            private final Long paymentId;
            private final Long amount;
            private final Long userId;
            private final UUID orderId;
        }
    }

    // -------------------------------------------------------------------------------------------------

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class GetTransactions {
        private final List<Item> items;

        public static GetTransactions from(List<PaymentGateway.Response.GetTransactions.Transaction> transactions) {
            return builder()
                    .items(transactions.stream()
                            .map(payment -> new Item(
                                    payment.transactionKey(),
                                    payment.status().name(),
                                    payment.reason()
                            ))
                            .toList()
                    )
                    .build();
        }

        @Getter
        @Builder
        @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Item {
            private final String transactionKey;
            private final String status;
            private final String reason;
        }
    }

    // -------------------------------------------------------------------------------------------------

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Ready {
        private final Long paymentId;
        private final PaymentStatus paymentStatus;
        private final PaymentMethod paymentMethod;

        public static Ready from(Payment payment) {
            return builder()
                    .paymentId(payment.getId())
                    .paymentStatus(payment.getStatus())
                    .paymentMethod(payment.getMethod())
                    .build();
        }
    }

    // -------------------------------------------------------------------------------------------------

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Pending {
        private final Long paymentId;
        private final PaymentStatus paymentStatus;

        public static Pending from(Payment payment) {
            return builder()
                    .paymentId(payment.getId())
                    .paymentStatus(payment.getStatus())
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
            return builder()
                    .paymentId(payment.getId())
                    .paymentStatus(payment.getStatus())
                    .build();
        }
    }

    // -------------------------------------------------------------------------------------------------

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Conclude {
        private final Long paymentId;
        private final PaymentStatus paymentStatus;

        public static Conclude from(Payment payment) {
            return builder()
                    .paymentId(payment.getId())
                    .paymentStatus(payment.getStatus())
                    .build();
        }
    }

}
