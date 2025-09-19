package com.loopers.domain.ranking;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record RankingCommand() {

    public record GetDaily(
            LocalDate startDate,
            LocalDate endDate
    ) {
    }

    // -------------------------------------------------------------------------------------------------

    public record AggregateDaily(
            LocalDate date,
            List<Map.Entry<Long, Double>> entries
    ) {
    }

    // -------------------------------------------------------------------------------------------------

    public record AggregateWeekly(
            LocalDate date,
            List<Long> productIds
    ) {
    }

    // -------------------------------------------------------------------------------------------------

    public record AggregateMonthly(
            LocalDate date,
            List<Long> productIds
    ) {
    }

}
