package com.loopers.interfaces.api.ranking;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.threeten.extra.YearWeek;

import java.time.LocalDate;
import java.time.YearMonth;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RankingRequest {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class SearchDaily {
        @PastOrPresent
        private final LocalDate date;
        @NotNull
        @Positive
        private final Integer page;
        @NotNull
        @Positive
        private final Integer size;
    }

    // -------------------------------------------------------------------------------------------------

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class SearchWeekly {
        @NotNull
        private final YearWeek yearWeek;
        @NotNull
        @Positive
        private final Integer page;
        @NotNull
        @Positive
        private final Integer size;
    }

    // -------------------------------------------------------------------------------------------------

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class SearchMonthly {
        @Past
        private final YearMonth yearMonth;
        @NotNull
        @Positive
        private final Integer page;
        @NotNull
        @Positive
        private final Integer size;
    }

}
