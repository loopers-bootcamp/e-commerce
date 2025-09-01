package com.loopers.interfaces.api.payment;

import com.loopers.interfaces.api.ApiHeader;
import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Payment V1 API", description = "결제 API V1")
public interface PaymentV1ApiSpec {

    @Operation(
            summary = "결제 요청",
            description = "결제를 요청합니다."
    )
    ApiResponse<PaymentResponse.Ready> ready(
            @Schema(name = ApiHeader.USER_ID, description = "로그인한 사용자의 ID")
            String userName,

            @Valid
            @RequestBody(description = "결제 정보")
            PaymentRequest.Ready request
    );

}
