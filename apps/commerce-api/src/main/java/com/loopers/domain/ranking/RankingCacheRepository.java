package com.loopers.domain.ranking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface RankingCacheRepository {

    List<RankingQueryResult.FindRanks> findRanks(LocalDate date);

    Page<RankingQueryResult.SearchRankings> searchRankings(LocalDate date, Pageable pageable);

    void saveRankings(LocalDate date, List<RankingQueryCommand.SaveRankings> rankings);

}
