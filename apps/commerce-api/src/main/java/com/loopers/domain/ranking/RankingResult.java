package com.loopers.domain.ranking;

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

}
