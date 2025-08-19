package com.loopers.infrastructure.payment.client;

import com.loopers.interfaces.api.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "pg-simulator-client", url = "${api-client.pg-simulator.base-url}")
public interface PgSimulatorClient {

    String HEADER_USER_ID = "X-USER-ID";

    @PostMapping(path = "/api/v1/payments", consumes = "application/json")
    ApiResponse<PgSimulatorResponse.RequestTransaction> requestTransaction(
            @RequestHeader(HEADER_USER_ID) Long userId,
            @RequestBody PgSimulatorRequest.RequestTransaction body
    );

    @GetMapping("/api/v1/payments")
    ApiResponse<PgSimulatorResponse.GetTransactions> getTransactions(
            @RequestHeader(HEADER_USER_ID) Long userId,
            @RequestParam UUID orderId
    );

    @GetMapping("/api/v1/payments/{transactionKey}")
    ApiResponse<PgSimulatorResponse.GetTransaction> getTransaction(
            @RequestHeader(HEADER_USER_ID) Long userId,
            @PathVariable String transactionKey
    );

}
