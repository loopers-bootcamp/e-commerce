package com.loopers.domain.payment;

import com.loopers.domain.payment.attribute.CardNumber;
import com.loopers.domain.payment.attribute.CardType;

import java.util.List;
import java.util.UUID;

public interface PaymentGateway {

    Response.Transact transact(Request.Transact request);

    Response.GetTransactions getTransactions(UUID orderId);

    // -------------------------------------------------------------------------------------------------

    record Request() {
        public record Transact(
                UUID orderId,
                CardType cardType,
                CardNumber cardNumber,
                Long amount
        ) {
        }
    }

    record Response() {
        public record Transact(
                String transactionKey,
                Status status,
                String reason
        ) {
        }

        public record GetTransactions(
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
    }

    enum Status {
        PENDING, SUCCESS, FAILED,
    }

}
