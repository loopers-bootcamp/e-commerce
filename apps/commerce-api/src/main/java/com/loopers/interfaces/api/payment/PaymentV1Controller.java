package com.loopers.interfaces.api.payment;

import com.loopers.application.payment.PaymentFacade;
import com.loopers.application.payment.PaymentInput;
import com.loopers.application.payment.PaymentOutput;
import com.loopers.interfaces.api.ApiHeader;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentV1Controller implements PaymentV1ApiSpec {

    private final PaymentFacade paymentFacade;

    @PostMapping
    @Override
    public ApiResponse<PaymentResponse.Ready> ready(
            @RequestHeader(ApiHeader.USER_ID)
            String userName,

            @RequestBody
            PaymentRequest.Ready request
    ) {
        PaymentInput.Ready input = PaymentInput.Ready.builder()
                .userName(userName)
                .orderId(request.getOrderId())
                .paymentMethod(request.getPaymentMethod())
                .cardType(request.getCardType())
                .cardNumber(request.getCardNumber())
                .build();
        PaymentOutput.Ready output = paymentFacade.ready(input);
        PaymentResponse.Ready response = PaymentResponse.Ready.from(output);

        return ApiResponse.success(response);
    }

}
