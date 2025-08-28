package com.loopers.domain.payment.event;

import java.util.UUID;

public record PaymentGatewayEvent() {

    public record Success(
            String transactionKey,
            UUID orderId,
            Long paymentId
    ) {
    }

    // -------------------------------------------------------------------------------------------------

    public record Failed(
            String transactionKey,
            String reason,
            UUID orderId,
            Long paymentId
    ) {
    }

    // -------------------------------------------------------------------------------------------------


}
