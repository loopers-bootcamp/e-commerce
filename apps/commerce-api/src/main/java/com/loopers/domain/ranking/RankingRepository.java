package com.loopers.domain.ranking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.threeten.extra.YearWeek;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;

public interface RankingRepository {

    Optional<Long> findRank(LocalDate date, Long productId);

    Page<RankingQueryResult.SearchRanks> searchRanks(LocalDate date, Pageable pageable);

    Page<RankingQueryResult.SearchRanks> searchRanks(YearWeek yearWeek, Pageable pageable);

    Page<RankingQueryResult.SearchRanks> searchRanks(YearMonth yearMonth, Pageable pageable);

}
