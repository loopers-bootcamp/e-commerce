package com.loopers.domain.ranking;

public record RankingQueryResult() {

    public record FindRanks(
            Long productId,
            Integer rank
    ) {
    }

}
