package com.loopers.interfaces.api.ranking;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDate;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RankingRequest {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class SearchRankings {
        @PastOrPresent
        private final LocalDate date;

        @NotNull
        @Positive
        private final Integer page;
        @NotNull
        @Positive
        private final Integer size;
    }

}
