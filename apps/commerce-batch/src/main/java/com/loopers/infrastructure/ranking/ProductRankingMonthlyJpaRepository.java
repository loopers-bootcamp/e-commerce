package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.ProductRankingMonthly;
import com.loopers.domain.ranking.ProductRankingWeekly;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.threeten.extra.YearWeek;

import java.time.YearMonth;
import java.time.ZonedDateTime;

public interface ProductRankingMonthlyJpaRepository extends JpaRepository<ProductRankingMonthly, Long> {

    @Modifying
    @Query("""
                insert into ProductRankingMonthly (yearMonth, productId, rank, createdAt, updatedAt)
                values (:yearMonth, :productId, :rank, :createdAt, :updatedAt)
                on conflict (yearMonth, productId) do update set
                             rank = :rank,
                             updatedAt = :updatedAt
            """)
    int merge(
            @Param("yearMonth") YearMonth yearMonth,
            @Param("productId") Long productId,
            @Param("rank") Integer rank,
            @Param("createdAt") ZonedDateTime createdAt,
            @Param("updatedAt") ZonedDateTime updatedAt
    );

}
