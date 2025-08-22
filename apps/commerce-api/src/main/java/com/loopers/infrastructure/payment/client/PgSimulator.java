package com.loopers.infrastructure.payment.client;

import com.loopers.domain.payment.PaymentGateway;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
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

    @Retry(name = "payment-gateway--request-transaction")
    @CircuitBreaker(name = "payment-gateway--request-transaction")
    @Override
    public Response.Transact transact(Request.Transact request) {
        String callbackUrl = UriComponentsBuilder.newInstance()
                .host(serverProperties.getAddress().getHostName())
                .port(serverProperties.getPort())
                .path("/callback/payments/{orderId}")
                .buildAndExpand(request.orderId())
                .toUriString();

        PgSimulatorRequest.RequestTransaction body = new PgSimulatorRequest.RequestTransaction(
                request.orderId(),
                request.cardType(),
                request.cardNumber().toFormattedString(),
                request.amount(),
                callbackUrl
        );
        PgApiResponse<PgSimulatorResponse.Transact> response = client.transact(storeId, body);

        return new Response.Transact(
                response.data().transactionKey(),
                Status.valueOf(response.data().status()),
                response.data().reason()
        );
    }

    @Retry(name = "payment-gateway--get-transactions")
    @CircuitBreaker(name = "payment-gateway--get-transactions")
    @Override
    public Response.GetTransactions getTransactions(UUID orderId) {
        PgApiResponse<PgSimulatorResponse.GetTransactions> response = client.getTransactions(storeId, orderId);

        return new Response.GetTransactions(
                UUID.fromString(response.data().orderId()),
                response.data().transactions().stream()
                        .map(tx -> new Response.GetTransactions.Transaction(
                                tx.transactionKey(),
                                Status.valueOf(tx.status()),
                                tx.reason()
                        ))
                        .toList()
        );
    }

}
