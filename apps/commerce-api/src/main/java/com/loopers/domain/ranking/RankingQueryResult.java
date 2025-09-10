package com.loopers.domain.ranking;

public record RankingQueryResult() {

    public record FindRanks(
            Long productId,
            Integer rank
    ) {
    }

    // -------------------------------------------------------------------------------------------------

    public record SearchRankings(
            Long productId,
            String productName,
            Integer basePrice,
            Long likeCount,
            Long brandId,
            String brandName
    ) {
    }

}
