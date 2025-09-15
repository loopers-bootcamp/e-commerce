package com.loopers.application.ranking;

import com.loopers.domain.product.ProductResult;
import com.loopers.domain.ranking.RankingResult;

import java.util.List;

public record RankingOutput() {

    public record SearchRankings(
            Integer totalPages,
            Long totalItems,
            Integer page,
            Integer size,
            List<Item> items
    ) {
        public static SearchRankings empty(RankingResult.SearchRanks result) {
            return new SearchRankings(result.totalPages(), result.totalItems(), result.page(), result.size(), List.of());
        }

        public static SearchRankings from(RankingResult.SearchRanks ranks, List<ProductResult.GetProductDetail> details) {
            return new SearchRankings(
                    ranks.totalPages(),
                    ranks.totalItems(),
                    ranks.page(),
                    ranks.size(),
                    details.stream()
                            .map(detail -> new Item(
                                    detail.productId(),
                                    detail.productName(),
                                    detail.basePrice(),
                                    detail.likeCount(),
                                    detail.brandId(),
                                    detail.brandName()
                            ))
                            .toList()
            );
        }

        public record Item(
                Long productId,
                String productName,
                Integer basePrice,
                Long likeCount,
                Long brandId,
                String brandName
        ) {
        }
    }

}
