package com.loopers.interfaces.api.order;

import com.loopers.interfaces.api.ApiHeader;
import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.UUID;

@Tag(name = "Order V1 API", description = "주문 API V1")
public interface OrderV1ApiSpec {

    @Operation(
            summary = "주문 상세 조회",
            description = "주문 상세 정보를 조회합니다."
    )
    ApiResponse<OrderResponse.GetOrderDetail> getOrderDetail(
            @Schema(name = ApiHeader.USER_ID, description = "로그인한 사용자의 ID")
            String userName,
            @Schema(name = "order id", description = "주문의 ID")
            UUID orderId
    );

    @Operation(
            summary = "주문 요청",
            description = "주문을 요청합니다."
    )
    ApiResponse<OrderResponse.Create> create(
            @Schema(name = ApiHeader.USER_ID, description = "로그인한 사용자의 ID")
            String userName,

            @Valid
            @RequestBody(description = "주문 정보")
            OrderRequest.Create request
    );

}
