package com.loopers.domain.ranking;

import lombok.*;

import java.time.LocalDate;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RankingCommand {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class SearchRankings {
        private final LocalDate date;
        private final Integer page;
        private final Integer size;
    }

}
