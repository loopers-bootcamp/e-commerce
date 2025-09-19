package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.ProductRankingWeekly;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.threeten.extra.YearWeek;

import java.time.ZonedDateTime;

public interface ProductRankingWeeklyJpaRepository extends JpaRepository<ProductRankingWeekly, Long> {

    @Modifying
    @Query("""
                insert into ProductRankingWeekly (yearWeek, productId, rank, createdAt, updatedAt)
                values (:yearWeek, :productId, :rank, :createdAt, :updatedAt)
                on conflict (yearWeek, productId) do update set
                             rank = :rank,
                             updatedAt = :updatedAt
            """)
    int merge(
            @Param("yearWeek") YearWeek yearWeek,
            @Param("productId") Long productId,
            @Param("rank") Integer rank,
            @Param("createdAt") ZonedDateTime createdAt,
            @Param("updatedAt") ZonedDateTime updatedAt
    );

}
