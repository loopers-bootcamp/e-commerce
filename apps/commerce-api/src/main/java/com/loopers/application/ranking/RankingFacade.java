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

    public RankingOutput.SearchRankings searchRankings(RankingInput.SearchRankings input) {
        RankingCommand.SearchRanks rankCommand = new RankingCommand.SearchRanks(input.date(), input.page(), input.size());
        RankingResult.SearchRanks ranks = rankingService.searchRanks(rankCommand);

        if (CollectionUtils.isEmpty(ranks.items())) {
            return RankingOutput.SearchRankings.empty(ranks);
        }

        List<ProductResult.GetProductDetail> details = ranks.items()
                .stream()
                .map(rank -> productService.getProductDetail(rank.productId())
                        .orElseThrow(() -> new BusinessException(CommonErrorType.NOT_FOUND))
                )
                .toList();

        return RankingOutput.SearchRankings.from(ranks, details);
    }

}
