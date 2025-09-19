package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.ProductRankingMonthly;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.YearMonth;

public interface ProductRankingMonthlyJpaRepository extends JpaRepository<ProductRankingMonthly, Long> {

    @Query("select p from ProductRankingMonthly p where p.yearMonth = ?1 order by p.rank, p.id")
    Page<ProductRankingMonthly> findByYearMonth(YearMonth yearMonth, Pageable pageable);

}
