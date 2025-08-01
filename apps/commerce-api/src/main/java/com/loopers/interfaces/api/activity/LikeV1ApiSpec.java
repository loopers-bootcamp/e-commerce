package com.loopers.interfaces.api.activity;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Like V1 API", description = "좋아요 API V1")
public interface LikeV1ApiSpec {

    @Operation(
            summary = "내 정보 조회",
            description = "아이디로 회원 정보를 조회합니다."
    )
    ApiResponse<Boolean> like(
            @Schema(name = "X-USER-ID", description = "조회할 사용자의 ID")
            String userName,
            @Schema(name = "product id", description = "상품 ID")
            Long productId
    );

    @Operation(
            summary = "회원 가입",
            description = "새로운 회원을 등록합니다."
    )
    ApiResponse<Boolean> dislike(
            @Schema(name = "X-USER-ID", description = "조회할 사용자의 ID")
            String userName,
            @Schema(name = "product id", description = "상품 ID")
            Long productId
    );

}
