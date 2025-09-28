package com.loopers.interfaces.api.ranking;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Ranking V1 API", description = "랭킹 API V1")
public interface RankingV1ApiSpec {

    @Operation(
            summary = "(실시간) 일간 상품 랭킹 조회",
            description = "(실시간) 일간 상품 랭킹을 조회합니다."
    )
    ApiResponse<RankingResponse.SearchDaily> searchDailyRankings(
            @Valid
            RankingRequest.SearchDaily request
    );

    @Operation(
            summary = "주간 상품 랭킹 조회",
            description = "주간 상품 랭킹을 조회합니다."
    )
    ApiResponse<RankingResponse.SearchWeekly> searchWeeklyRankings(
            @Valid
            RankingRequest.SearchWeekly request
    );

    @Operation(
            summary = "월간 상품 랭킹 조회",
            description = "월간 상품 랭킹을 조회합니다."
    )
    ApiResponse<RankingResponse.SearchMonthly> searchMonthlyRankings(
            @Valid
            RankingRequest.SearchMonthly request
    );

}
