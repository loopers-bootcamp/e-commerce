package com.loopers.domain.payment;

import com.loopers.domain.payment.attribute.CardNumber;
import com.loopers.domain.payment.attribute.CardType;

import java.util.List;
import java.util.UUID;

public interface PaymentGateway {

    RequestTransaction requestTransaction(
            UUID orderId,
            CardType cardType,
            CardNumber cardNumber,
            Long amount
    );

    GetTransactions getTransactions(UUID orderId);

    // -------------------------------------------------------------------------------------------------

    record RequestTransaction(
            String transactionKey,
            Status status,
            String reason
    ) {
    }

    record GetTransactions(
            UUID orderId,
            List<Transaction> transactions
    ) {
        public record Transaction(
                String transactionKey,
                Status status,
                String reason
        ) {
        }
    }

    enum Status {
        PENDING, SUCCESS, FAILED,
    }

}
