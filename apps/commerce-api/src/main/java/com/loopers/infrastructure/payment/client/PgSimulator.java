package com.loopers.infrastructure.payment.client;

import com.loopers.domain.payment.PaymentGateway;
import com.loopers.domain.payment.attribute.CardNumber;
import com.loopers.domain.payment.attribute.CardType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PgSimulator implements PaymentGateway {

    private final PgSimulatorClient client;
    private final ServerProperties serverProperties;

    @Value("${pg-simulator.store-id}")
    private final String storeId;

    @Override
    public PaymentGateway.RequestTransaction requestTransaction(
            UUID orderId,
            CardType cardType,
            CardNumber cardNumber,
            Long amount
    ) {
        // TODO: set circuit breaker + retry(on GET)
        String callbackUrl = UriComponentsBuilder.newInstance()
                .host(serverProperties.getAddress().getHostName())
                .port(serverProperties.getPort())
                .path("/callback/payments/{orderId}")
                .buildAndExpand(orderId)
                .toUriString();

        PgSimulatorRequest.RequestTransaction body = new PgSimulatorRequest.RequestTransaction(
                orderId, cardType, cardNumber.toFormattedString(), amount, callbackUrl);
        PgApiResponse<PgSimulatorResponse.RequestTransaction> response = client.requestTransaction(storeId, body);

        return new PaymentGateway.RequestTransaction(
                response.data().transactionKey(),
                PaymentGateway.Status.valueOf(response.data().status()),
                response.data().reason()
        );
    }

    @Override
    public PaymentGateway.GetTransactions getTransactions(UUID orderId) {
        // TODO: set circuit breaker + retry(on GET)
        PgApiResponse<PgSimulatorResponse.GetTransactions> response = client.getTransactions(storeId, orderId);

        return new PaymentGateway.GetTransactions(
                UUID.fromString(response.data().orderId()),
                response.data().transactions().stream()
                        .map(tx -> new PaymentGateway.GetTransactions.Transaction(
                                tx.transactionKey(),
                                PaymentGateway.Status.valueOf(tx.status()),
                                tx.reason()
                        ))
                        .toList()
        );
    }

}
