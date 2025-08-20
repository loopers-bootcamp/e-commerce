package com.loopers.domain.payment;

import com.loopers.domain.payment.attribute.CardNumber;
import com.loopers.domain.payment.attribute.CardType;
import com.loopers.infrastructure.payment.client.PgSimulatorResponse;

import java.util.UUID;

public interface PaymentGateway {

    PgSimulatorResponse.RequestTransaction requestTransaction(
            UUID orderId,
            CardType cardType,
            CardNumber cardNumber,
            Long amount
    );

}
