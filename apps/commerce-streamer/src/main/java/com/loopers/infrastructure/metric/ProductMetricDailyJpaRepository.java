package com.loopers.infrastructure.metric;

import com.loopers.domain.metric.ProductMetricDaily;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public interface ProductMetricDailyJpaRepository extends JpaRepository<ProductMetricDaily, Long> {

    @Modifying
    @Query("""
                insert into ProductMetricDaily (date, likeCount, saleQuantity, viewCount, productId, createdAt, updatedAt)
                values (:date, :likeCount, :saleQuantity, :viewCount, :productId, :createdAt, :updatedAt)
                on conflict (date, productId) do update set
                             likeCount = likeCount + :likeCount,
                             saleQuantity = saleQuantity + :saleQuantity,
                             viewCount = viewCount + :viewCount,
                             updatedAt = :updatedAt
            """)
    int merge(
            @Param("date") LocalDate date,
            @Param("likeCount") Long likeCount,
            @Param("saleQuantity") Long saleQuantity,
            @Param("viewCount") Long viewCount,
            @Param("productId") Long productId,
            @Param("createdAt") ZonedDateTime createdAt,
            @Param("updatedAt") ZonedDateTime updatedAt
    );

}
