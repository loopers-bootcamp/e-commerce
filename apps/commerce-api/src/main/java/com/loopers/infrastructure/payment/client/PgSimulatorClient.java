package com.loopers.infrastructure.payment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "pg-simulator-client", url = "${pg-simulator.api-client.base-url}/api/v1/payments")
public interface PgSimulatorClient {

    String HEADER_STORE_ID = "X-USER-ID";

    @PostMapping
    PgApiResponse<PgSimulatorResponse.Transact> transact(
            @RequestHeader(HEADER_STORE_ID) String storeId,
            @RequestBody PgSimulatorRequest.RequestTransaction body
    );

    @GetMapping
    PgApiResponse<PgSimulatorResponse.GetTransactions> getTransactions(
            @RequestHeader(HEADER_STORE_ID) String storeId,
            @RequestParam(name = "orderId") UUID orderId
    );

    @GetMapping("/{transactionKey}")
    PgApiResponse<PgSimulatorResponse.GetTransaction> getTransaction(
            @RequestHeader(HEADER_STORE_ID) String storeId,
            @PathVariable String transactionKey
    );

}
