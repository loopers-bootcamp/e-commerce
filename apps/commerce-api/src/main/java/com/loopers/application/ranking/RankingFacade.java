package com.loopers.application.ranking;

import com.loopers.domain.product.ProductResult;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.ranking.RankingCommand;
import com.loopers.domain.ranking.RankingResult;
import com.loopers.domain.ranking.RankingService;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RankingFacade {

    private final RankingService rankingService;
    private final ProductService productService;

    public RankingOutput.SearchDaily searchDaily(RankingInput.SearchDaily input) {
        RankingResult.SearchDaily ranks = rankingService.searchDaily(new RankingCommand.SearchDaily(
                input.date(),
                input.page(),
                input.size()
        ));

        if (CollectionUtils.isEmpty(ranks.items())) {
            return RankingOutput.SearchDaily.empty(ranks);
        }

        List<ProductResult.GetProductDetail> details = ranks.items()
                .stream()
                .map(rank -> productService.getProductDetail(rank.productId())
                        .orElseThrow(() -> new BusinessException(CommonErrorType.NOT_FOUND))
                )
                .toList();

        return RankingOutput.SearchDaily.from(ranks, details);
    }

    public RankingOutput.SearchWeekly searchWeekly(RankingInput.SearchWeekly input) {
        RankingResult.SearchWeekly ranks = rankingService.searchWeekly(new RankingCommand.SearchWeekly(
                input.yearWeek(),
                input.page(),
                input.size()
        ));

        if (CollectionUtils.isEmpty(ranks.items())) {
            return RankingOutput.SearchWeekly.empty(ranks);
        }

        List<ProductResult.GetProductDetail> details = ranks.items()
                .stream()
                .map(rank -> productService.getProductDetail(rank.productId())
                        .orElseThrow(() -> new BusinessException(CommonErrorType.NOT_FOUND))
                )
                .toList();

        return RankingOutput.SearchWeekly.from(ranks, details);
    }

    public RankingOutput.SearchMonthly searchMonthly(RankingInput.SearchMonthly input) {
        RankingResult.SearchMonthly ranks = rankingService.searchMonthly(new RankingCommand.SearchMonthly(
                input.yearMonth(),
                input.page(),
                input.size()
        ));

        if (CollectionUtils.isEmpty(ranks.items())) {
            return RankingOutput.SearchMonthly.empty(ranks);
        }

        List<ProductResult.GetProductDetail> details = ranks.items()
                .stream()
                .map(rank -> productService.getProductDetail(rank.productId())
                        .orElseThrow(() -> new BusinessException(CommonErrorType.NOT_FOUND))
                )
                .toList();

        return RankingOutput.SearchMonthly.from(ranks, details);
    }

}
