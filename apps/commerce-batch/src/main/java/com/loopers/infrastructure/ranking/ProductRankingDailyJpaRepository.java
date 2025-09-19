package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.ProductRankingDaily;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

public interface ProductRankingDailyJpaRepository extends JpaRepository<ProductRankingDaily, Long> {

    List<ProductRankingDaily> findByDateBetween(LocalDate start, LocalDate end);

    @Modifying
    @Query("""
                insert into ProductRankingDaily (date, productId, rank, createdAt, updatedAt)
                values (:date, :productId, :rank, :createdAt, :updatedAt)
                on conflict (date, productId) do update set
                             rank = :rank,
                             updatedAt = :updatedAt
            """)
    int merge(
            @Param("date") LocalDate date,
            @Param("productId") Long productId,
            @Param("rank") Integer rank,
            @Param("createdAt") ZonedDateTime createdAt,
            @Param("updatedAt") ZonedDateTime updatedAt
    );

}
