package com.loopers.domain.ranking;

import java.time.LocalDate;
import java.util.List;

public interface RankingRepository {

    List<ProductRankingDaily> findDailyRanking(LocalDate startDate, LocalDate endDate);

    boolean merge(ProductRankingDaily ranking);

    boolean merge(ProductRankingWeekly ranking);

    boolean merge(ProductRankingMonthly ranking);

}
