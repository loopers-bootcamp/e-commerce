package com.loopers.infrastructure.payment.client;

import com.loopers.domain.payment.PaymentGateway;
import com.loopers.domain.payment.attribute.CardNumber;
import com.loopers.domain.payment.attribute.CardType;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PgSimulator implements PaymentGateway {

    private final PgSimulatorClient client;
    private final ServerProperties serverProperties;

    @Value("${pg-simulator.store-id}")
    private final String storeId;

    @Override
    public PgSimulatorResponse.RequestTransaction requestTransaction(
            UUID orderId,
            CardType cardType,
            CardNumber cardNumber,
            Long amount
    ) {
        // TODO: set circuit breaker + retry(on GET)
        String hostName = serverProperties.getAddress().getHostName();
        Integer port = serverProperties.getPort();
        String callbackUrl = "http://%s:%d".formatted(hostName, port);

        PgSimulatorRequest.RequestTransaction body = new PgSimulatorRequest.RequestTransaction(
                orderId, cardType, cardNumber.toFormattedString(), amount, callbackUrl);
        ApiResponse<PgSimulatorResponse.RequestTransaction> response = client.requestTransaction(storeId, body);

        return response.data();
    }

}
