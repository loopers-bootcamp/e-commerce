package com.loopers.interfaces.callback.payment;

import com.loopers.application.payment.PaymentFacade;
import com.loopers.application.payment.PaymentInput;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/callback/payments")
public class PaymentCallbackController {

    private final PaymentFacade paymentFacade;

    @PostMapping("/{orderId}")
    public ApiResponse<Boolean> processTransaction(
            @PathVariable
            UUID orderId,

            @RequestBody
            PaymentCallbackRequest.ProcessTransaction request
    ) {
        PaymentInput.Conclude input = PaymentInput.Conclude.builder()
                .transactionKey(request.getTransactionKey())
                .orderId(orderId)
                .amount(request.getAmount())
                .status(request.getStatus())
                .reason(request.getReason())
                .build();
        paymentFacade.conclude(input);

        return ApiResponse.success(true);
    }

}
