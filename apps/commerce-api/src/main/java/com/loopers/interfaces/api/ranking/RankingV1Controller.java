package com.loopers.interfaces.api.ranking;

import com.loopers.domain.ranking.RankingCommand;
import com.loopers.domain.ranking.RankingResult;
import com.loopers.domain.ranking.RankingService;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/rankings")
public class RankingV1Controller implements RankingV1ApiSpec {

    private final RankingService rankingService;

    @GetMapping
    @Override
    public ApiResponse<RankingResponse.SearchRankings> searchRankings(
            RankingRequest.SearchRankings request
    ) {
        RankingCommand.SearchRankings command = RankingCommand.SearchRankings.builder()
                .date(request.getDate())
                .page(request.getPage())
                .size(request.getSize())
                .build();
        RankingResult.SearchRankings rankings = rankingService.searchRankings(command);
        RankingResponse.SearchRankings response = RankingResponse.SearchRankings.from(rankings);

        return ApiResponse.success(response);
    }

}
