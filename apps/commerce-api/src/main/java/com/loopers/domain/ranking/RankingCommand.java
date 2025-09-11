package com.loopers.domain.ranking;

import java.time.LocalDate;

public record RankingCommand() {

    public record FindRank(
            LocalDate date,
            Long productId
    ) {
    }

    // -------------------------------------------------------------------------------------------------

    public record SearchRanks(
            LocalDate date,
            Integer page,
            Integer size
    ) {
    }

}
