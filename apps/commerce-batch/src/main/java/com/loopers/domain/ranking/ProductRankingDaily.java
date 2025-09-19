package com.loopers.domain.ranking;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Entity
@Table(
        name = "product_rankings_daily",
        uniqueConstraints = @UniqueConstraint(columnNames = {"date", "ref_product_id"}),
        indexes = @Index(columnList = "date, ref_product_id, rank")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductRankingDaily extends BaseEntity {

    /**
     * 아이디
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_ranking_daily_id", nullable = false, updatable = false)
    private Long id;

    /**
     * 기준일자
     */
    @Column(name = "date", nullable = false, updatable = false)
    private LocalDate date;

    /**
     * 순위
     */
    @Column(name = "rank", nullable = false, updatable = false)
    private Integer rank;

    /**
     * 점수
     */
    @Column(name = "score", nullable = false, updatable = false)
    private Double score;

    // -------------------------------------------------------------------------------------------------

    /**
     * 상품 아이디
     */
    @Column(name = "ref_product_id", nullable = false, updatable = false)
    private Long productId;

    // -------------------------------------------------------------------------------------------------

    @Builder
    private ProductRankingDaily(
            LocalDate date,
            Integer rank,
            Double score,
            Long productId
    ) {
        if (date == null) {
            throw new IllegalArgumentException("기준 일자가 올바르지 않습니다.");
        }
        if (rank == null || rank <= 0) {
            throw new IllegalArgumentException("랭크가 올바르지 않습니다.");
        }
        if (score == null || Double.isNaN(score) || Double.isInfinite(score) || score <= 0.0) {
            throw new IllegalArgumentException("점수가 올바르지 않습니다.");
        }
        if (productId == null) {
            throw new IllegalArgumentException("상품 아이디가 올바르지 않습니다.");
        }

        this.date = date;
        this.rank = rank;
        this.score = score;
        this.productId = productId;
    }

}
