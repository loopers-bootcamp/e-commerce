package com.loopers.domain.ranking;

import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

public interface RankingCacheRepository {

    List<RankingQueryResult.FindRanks> findRanks(LocalDate date);

    Page<?> searchRankings(
            LocalDate date,
            Integer page,
            Integer size
    );

}
