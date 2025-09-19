package com.loopers.domain.ranking;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.YearMonth;

@Getter
@Entity
@Table(
        name = "product_rankings_monthly",
        uniqueConstraints = @UniqueConstraint(columnNames = {"year_month", "ref_product_id"}),
        indexes = @Index(columnList = "year_month, ref_product_id, rank")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductRankingMonthly extends BaseEntity {

    /**
     * 아이디
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_ranking_weekly_id", nullable = false, updatable = false)
    private Long id;

    /**
     * 한 해의 주차
     */
    @Column(name = "year_month", nullable = false, updatable = false)
    private YearMonth yearMonth;

    /**
     * 순위
     */
    @Column(name = "rank", nullable = false, updatable = false)
    private Integer rank;

    // -------------------------------------------------------------------------------------------------

    /**
     * 상품 아이디
     */
    @Column(name = "ref_product_id", nullable = false, updatable = false)
    private Long productId;

}
