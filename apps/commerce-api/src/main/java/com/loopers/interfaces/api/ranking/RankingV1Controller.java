package com.loopers.interfaces.api.ranking;

import com.loopers.application.ranking.RankingFacade;
import com.loopers.application.ranking.RankingInput;
import com.loopers.application.ranking.RankingOutput;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/rankings")
public class RankingV1Controller implements RankingV1ApiSpec {

    private final RankingFacade rankingFacade;

    @GetMapping
    @Override
    public ApiResponse<RankingResponse.SearchRankings> searchRankings(
            RankingRequest.SearchRankings request
    ) {
        RankingInput.SearchRankings input = new RankingInput.SearchRankings(
                request.getDate(),
                request.getPage(),
                request.getSize()
        );
        RankingOutput.SearchRankings rankings = rankingFacade.searchRankings(input);
        RankingResponse.SearchRankings response = RankingResponse.SearchRankings.from(rankings);

        return ApiResponse.success(response);
    }

}
