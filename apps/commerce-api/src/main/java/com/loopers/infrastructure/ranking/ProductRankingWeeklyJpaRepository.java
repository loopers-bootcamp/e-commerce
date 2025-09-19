package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.ProductRankingWeekly;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.threeten.extra.YearWeek;

public interface ProductRankingWeeklyJpaRepository extends JpaRepository<ProductRankingWeekly, Long> {

    @Query("select p from ProductRankingWeekly p where p.yearWeek = ?1 order by p.rank, p.id")
    Page<ProductRankingWeekly> findByYearWeek(YearWeek yearWeek, Pageable pageable);

}
