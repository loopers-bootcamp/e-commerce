package com.loopers.domain.ranking;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.threeten.extra.YearWeek;

@Getter
@Entity
@Table(
        name = "product_rankings_weekly",
        uniqueConstraints = @UniqueConstraint(columnNames = {"year_week", "ref_product_id"}),
        indexes = @Index(columnList = "year_week, ref_product_id, rank")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductRankingWeekly extends BaseEntity {

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
    @Column(name = "year_week", nullable = false, updatable = false)
    private YearWeek yearWeek;

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
