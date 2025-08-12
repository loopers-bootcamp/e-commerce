package com.loopers.interfaces.api.product;

import com.loopers.interfaces.api.ApiHeader;
import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

@Tag(name = "Product V1 API", description = "상품 API V1")
public interface ProductV1ApiSpec {

    @Operation(
            summary = "상품 목록 조회",
            description = "상품 목록을 조회합니다."
    )
    ApiResponse<ProductResponse.SearchProducts> searchProducts(
            @Valid
            ProductRequest.SearchProducts request
    );

    @Operation(
            summary = "상품 상세 조회",
            description = "상품 상세 정보를 조회합니다."
    )
    ApiResponse<ProductResponse.GetProductDetail> getProductDetail(
            @Schema(name = ApiHeader.USER_ID, description = "조회할 사용자의 ID", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
            String userName,
            @Schema(name = "product id", description = "상품의 ID") @Positive
            Long productId
    );

}
