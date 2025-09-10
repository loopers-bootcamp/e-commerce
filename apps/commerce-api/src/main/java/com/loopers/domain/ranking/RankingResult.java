package com.loopers.domain.ranking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public record RankingResult() {

    public record FindRanks(
            List<Item> items
    ) {
        public static FindRanks from(List<RankingQueryResult.FindRanks> results) {
            List<Item> items = results.stream()
                    .map(result -> new Item(result.productId(), result.rank()))
                    .toList();

            return new FindRanks(items);
        }

        public record Item(
                Long productId,
                Integer rank
        ) {
        }
    }

    // -------------------------------------------------------------------------------------------------

    public record SearchRankings(
            Integer totalPages,
            Long totalItems,
            Integer page,
            Integer size,
            List<Item> items
    ) {
        public static SearchRankings from(Page<RankingQueryResult.SearchRankings> page, Pageable pageable) {
            List<Item> items = page.map(result -> new Item(
                            result.productId(),
                            result.productName(),
                            result.basePrice(),
                            result.likeCount(),
                            result.brandId(),
                            result.brandName()
                    ))
                    .toList();

            return new SearchRankings(
                    page.getTotalPages(),
                    page.getTotalElements(),
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    items
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
