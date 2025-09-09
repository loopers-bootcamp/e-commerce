package com.loopers.interfaces.api.ranking;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Ranking V1 API", description = "랭킹 API V1")
public interface RankingV1ApiSpec {

    @Operation(
            summary = "상품 목록 조회",
            description = "상품 목록을 조회합니다."
    )
    ApiResponse<RankingResponse.SearchRankings> searchRankings(
            @Valid
            RankingRequest.SearchRankings request
    );

}
