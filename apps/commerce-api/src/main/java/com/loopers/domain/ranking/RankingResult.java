package com.loopers.domain.ranking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public record RankingResult() {

    public record SearchRanks(
            Integer totalPages,
            Long totalItems,
            Integer page,
            Integer size,
            List<Item> items
    ) {
        public static SearchRanks from(Page<RankingQueryResult.SearchRanks> page, Pageable pageable) {
            return new SearchRanks(
                    page.getTotalPages(),
                    page.getTotalElements(),
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    page.map(result -> new Item(result.productId(), result.rank())).toList()
            );
        }

        public Pageable pageable() {
            return PageRequest.of(page, size);
        }

        public record Item(
                Long productId,
                Integer rank
        ) {
        }
    }

}
