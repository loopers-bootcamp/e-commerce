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

    @GetMapping("/daily")
    @Override
    public ApiResponse<RankingResponse.SearchDaily> searchDailyRankings(
            RankingRequest.SearchDaily request
    ) {
        RankingInput.SearchDaily input = new RankingInput.SearchDaily(
                request.getDate(),
                request.getPage(),
                request.getSize()
        );
        RankingOutput.SearchDaily rankings = rankingFacade.searchDaily(input);
        RankingResponse.SearchDaily response = RankingResponse.SearchDaily.from(rankings);

        return ApiResponse.success(response);
    }

    @GetMapping("/weekly")
    @Override
    public ApiResponse<RankingResponse.SearchWeekly> searchWeeklyRankings(
            RankingRequest.SearchWeekly request
    ) {
        RankingInput.SearchWeekly input = new RankingInput.SearchWeekly(
                request.getYearWeek(),
                request.getPage(),
                request.getSize()
        );
        RankingOutput.SearchWeekly rankings = rankingFacade.searchWeekly(input);
        RankingResponse.SearchWeekly response = RankingResponse.SearchWeekly.from(rankings);

        return ApiResponse.success(response);
    }

    @GetMapping("/monthly")
    @Override
    public ApiResponse<RankingResponse.SearchMonthly> searchMonthlyRankings(
            RankingRequest.SearchMonthly request
    ) {
        RankingInput.SearchMonthly input = new RankingInput.SearchMonthly(
                request.getYearMonth(),
                request.getPage(),
                request.getSize()
        );
        RankingOutput.SearchMonthly rankings = rankingFacade.searchMonthly(input);
        RankingResponse.SearchMonthly response = RankingResponse.SearchMonthly.from(rankings);

        return ApiResponse.success(response);
    }

}
