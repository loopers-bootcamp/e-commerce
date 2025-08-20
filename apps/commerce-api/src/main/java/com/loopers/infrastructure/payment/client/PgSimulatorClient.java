package com.loopers.infrastructure.payment.client;

import com.loopers.interfaces.api.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "pg-simulator-client", url = "${pg-simulator.api-client.base-url}")
public interface PgSimulatorClient {

    String HEADER_STORE_ID = "X-USER-ID";

    @PostMapping(path = "/api/v1/payments", consumes = "application/json")
    ApiResponse<PgSimulatorResponse.RequestTransaction> requestTransaction(
            @RequestHeader(HEADER_STORE_ID) String storeId,
            @RequestBody PgSimulatorRequest.RequestTransaction body
    );

    @GetMapping("/api/v1/payments")
    ApiResponse<PgSimulatorResponse.GetTransactions> getTransactions(
            @RequestHeader(HEADER_STORE_ID) String storeId,
            @RequestParam UUID orderId
    );

    @GetMapping("/api/v1/payments/{transactionKey}")
    ApiResponse<PgSimulatorResponse.GetTransaction> getTransaction(
            @RequestHeader(HEADER_STORE_ID) String storeId,
            @PathVariable String transactionKey
    );

}
