package com.loopers.infrastructure.payment.client;

import java.util.List;

public class PgSimulatorResponse {

    public record RequestTransaction(
            String transactionKey,
            String status,
            String reason
    ) {
    }

    // -------------------------------------------------------------------------------------------------

    public record GetTransactions(
            String orderId,
            List<Transaction> transactions
    ) {
        public record Transaction(
                String transactionKey,
                String status,
                String reason
        ) {
        }
    }

    // -------------------------------------------------------------------------------------------------

    public record GetTransaction(
            String transactionKey,
            String orderId,
            String cardType,
            String cardNo,
            Long amount,
            String status,
            String reason
    ) {
    }

}
