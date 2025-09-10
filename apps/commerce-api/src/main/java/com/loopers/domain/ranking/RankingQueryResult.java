package com.loopers.domain.ranking;

public record RankingQueryResult() {

    public record SearchRanks(
            Long productId,
            Integer rank
    ) {
    }

}
