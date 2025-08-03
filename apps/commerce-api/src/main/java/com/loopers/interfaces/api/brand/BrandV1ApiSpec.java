package com.loopers.interfaces.api.brand;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;

@Tag(name = "Brand V1 API", description = "브랜드 API V1")
public interface BrandV1ApiSpec {

    @Operation(
            summary = "브랜드 정보 조회",
            description = "브랜드 정보를 조회합니다."
    )
    ApiResponse<BrandResponse.GetBrand> getBrand(
            @Schema(name = "brand id", description = "브랜드의 ID") @Positive
            Long brandId
    );

}
