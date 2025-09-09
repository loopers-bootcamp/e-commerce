package com.loopers.application.ranking;

import com.loopers.domain.product.ProductResult;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.ranking.RankingResult;
import com.loopers.domain.ranking.RankingService;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RankingFacade {

    private final RankingService rankingService;
    private final ProductService productService;

    public void saveDailyRankings(LocalDate date) {
        // TODO: carry-over
        RankingResult.FindRanks ranks = rankingService.findRanks(date);
        if (CollectionUtils.isEmpty(ranks.items())) {
            return;
        }

        int maxRanks = Math.min(ranks.items().size(), 1000);
        List<RankingResult.FindRanks.Item> top1k = ranks.items().subList(0, maxRanks);

        for (RankingResult.FindRanks.Item item : top1k) {
            ProductResult.GetProductDetail detail = productService.getProductDetail(item.productId())
                    .orElseThrow(() -> new BusinessException(CommonErrorType.NOT_FOUND));
        }
    }

}
