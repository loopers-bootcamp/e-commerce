package com.loopers.interfaces.api.activity;

import com.loopers.interfaces.api.ApiHeader;
import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;

@Tag(name = "Like V1 API", description = "좋아요 API V1")
public interface LikeV1ApiSpec {

    @Operation(
            summary = "좋아요 상품 목록 조회",
            description = "내가 좋아요 한 상품 목록을 조회합니다."
    )
    ApiResponse<LikeResponse.GetLikedProducts> getLikedProducts(
            @Schema(name = ApiHeader.USER_ID, description = "로그인한 사용자의 ID")
            String userName
    );

    @Operation(
            summary = "상품 좋아요 등록",
            description = "상품에 좋아요를 등록합니다."
    )
    ApiResponse<Boolean> like(
            @Schema(name = ApiHeader.USER_ID, description = "로그인한 사용자의 ID")
            String userName,
            @Schema(name = "product id", description = "상품 ID") @Positive
            Long productId
    );

    @Operation(
            summary = "상품 좋아요 취소",
            description = "상품에 등록한 좋아요를 취소합니다."
    )
    ApiResponse<Boolean> dislike(
            @Schema(name = ApiHeader.USER_ID, description = "로그인한 사용자의 ID")
            String userName,
            @Schema(name = "product id", description = "상품 ID") @Positive
            Long productId
    );

}
