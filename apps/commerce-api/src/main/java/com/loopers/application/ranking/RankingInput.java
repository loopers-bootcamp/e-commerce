package com.loopers.application.ranking;

import org.threeten.extra.YearWeek;

import java.time.LocalDate;
import java.time.YearMonth;

public record RankingInput() {

    public record SearchDaily(
            LocalDate date,
            Integer page,
            Integer size
    ) {
    }

    // -------------------------------------------------------------------------------------------------

    public record SearchWeekly(
            YearWeek yearWeek,
            Integer page,
            Integer size
    ) {
    }

    // -------------------------------------------------------------------------------------------------

    public record SearchMonthly(
            YearMonth yearMonth,
            Integer page,
            Integer size
    ) {
    }

}
