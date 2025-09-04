package com.loopers.domain.metric;

import com.loopers.domain.BaseEntity;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Objects;

@Getter
@Entity
@Table(
        name = "product_metrics_daily",
        uniqueConstraints = @UniqueConstraint(columnNames = {"metric_date", "ref_product_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductMetricDaily extends BaseEntity {

    /**
     * 아이디
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "metric_id", nullable = false, updatable = false)
    private Long id;

    /**
     * 기준 일자
     */
    @Column(name = "metric_date", nullable = false, updatable = false)
    private LocalDate date;

    /**
     * 상품 좋아요 수
     */
    @Column(name = "like_count", nullable = false)
    private Long likeCount;

    /**
     * 상품 판매 수
     */
    @Column(name = "sale_quantity", nullable = false)
    private Long saleQuantity;

    /**
     * 상품 조회 수
     */
    @Column(name = "view_count", nullable = false)
    private Long viewCount;

    // -------------------------------------------------------------------------------------------------

    /**
     * 상품 아이디
     */
    @Column(name = "ref_product_id", nullable = false, updatable = false)
    private Long productId;

    // -------------------------------------------------------------------------------------------------

    @Builder
    private ProductMetricDaily(
            LocalDate date,
            @Nullable Long likeCount,
            @Nullable Long saleQuantity,
            @Nullable Long viewCount,
            Long productId
    ) {
        if (date == null) {
            throw new IllegalArgumentException("기준 일자가 올바르지 않습니다.");
        }
        if (viewCount != null && viewCount < 0) {
            throw new IllegalArgumentException("상품 조회 수가 올바르지 않습니다.");
        }
        if (productId == null) {
            throw new IllegalArgumentException("상품 아이디가 올바르지 않습니다.");
        }

        this.date = date;
        this.likeCount = Objects.requireNonNullElse(likeCount, 0L);
        this.saleQuantity = Objects.requireNonNullElse(saleQuantity, 0L);
        this.viewCount = Objects.requireNonNullElse(viewCount, 0L);
        this.productId = productId;
    }

}
