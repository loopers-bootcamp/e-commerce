package com.loopers.application.ranking;

import java.time.LocalDate;

public record RankingInput() {

    public record SearchRankings(
            LocalDate date,
            Integer page,
            Integer size
    ) {
    }

}
