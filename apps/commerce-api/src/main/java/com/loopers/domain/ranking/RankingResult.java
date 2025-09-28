package com.loopers.domain.ranking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public record RankingResult() {

    public record SearchDaily(
            Integer totalPages,
            Long totalItems,
            Integer page,
            Integer size,
            List<Item> items
    ) {
        public static SearchDaily from(Page<RankingQueryResult.SearchRanks> page, Pageable pageable) {
            return new SearchDaily(
                    page.getTotalPages(),
                    page.getTotalElements(),
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    page.map(result -> new Item(result.productId(), result.rank())).toList()
            );
        }

        public record Item(
                Long productId,
                Long rank
        ) {
        }
    }

    // -------------------------------------------------------------------------------------------------

    public record SearchWeekly(
            Integer totalPages,
            Long totalItems,
            Integer page,
            Integer size,
            List<Item> items
    ) {
        public static SearchWeekly from(Page<RankingQueryResult.SearchRanks> page, Pageable pageable) {
            return new SearchWeekly(
                    page.getTotalPages(),
                    page.getTotalElements(),
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    page.map(result -> new Item(result.productId(), result.rank())).toList()
            );
        }

        public record Item(
                Long productId,
                Long rank
        ) {
        }
    }

    // -------------------------------------------------------------------------------------------------

    public record SearchMonthly(
            Integer totalPages,
            Long totalItems,
            Integer page,
            Integer size,
            List<Item> items
    ) {
        public static SearchMonthly from(Page<RankingQueryResult.SearchRanks> page, Pageable pageable) {
            return new SearchMonthly(
                    page.getTotalPages(),
                    page.getTotalElements(),
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    page.map(result -> new Item(result.productId(), result.rank())).toList()
            );
        }

        public record Item(
                Long productId,
                Long rank
        ) {
        }
    }

}
