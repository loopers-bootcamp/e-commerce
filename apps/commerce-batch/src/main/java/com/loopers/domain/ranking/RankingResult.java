package com.loopers.domain.ranking;

import java.time.LocalDate;
import java.util.List;

public record RankingResult() {

    public record GetDaily(
            List<Item> items
    ) {
        public static GetDaily from(List<ProductRankingDaily> rankings) {
            List<Item> items = rankings.stream()
                    .map(ranking -> new Item(
                            ranking.getDate(),
                            ranking.getProductId(),
                            ranking.getRank(),
                            ranking.getScore()
                    ))
                    .toList();

            return new GetDaily(items);
        }

        public record Item(
                LocalDate date,
                Long productId,
                Integer rank,
                Double score
        ) {
        }
    }

}
