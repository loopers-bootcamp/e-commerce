package com.loopers.domain.ranking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface RankingRepository {

    Page<RankingQueryResult.SearchRanks> searchRanks(LocalDate date, Pageable pageable);

}
