package com.loopers.domain.metric;

import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Objects;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Metric {

    /**
     * 기준 일자
     */
    @EqualsAndHashCode.Include
    private final LocalDate date;

    /**
     * 상품 아이디
     */
    @EqualsAndHashCode.Include
    private final Long productId;

    /**
     * 상품 좋아요 수
     */
    private final Long likeCount;

    /**
     * 상품 판매 수
     */
    private final Long saleQuantity;

    /**
     * 상품 조회 수
     */
    private final Long viewCount;

    // -------------------------------------------------------------------------------------------------

    @Builder
    private Metric(
            LocalDate date,
            Long productId,
            @Nullable Long likeCount,
            @Nullable Long saleQuantity,
            @Nullable Long viewCount
    ) {
        if (date == null) {
            throw new IllegalArgumentException("기준 일자가 올바르지 않습니다.");
        }
        if (productId == null) {
            throw new IllegalArgumentException("상품 아이디가 올바르지 않습니다.");
        }
        if (viewCount != null && viewCount < 0) {
            throw new IllegalArgumentException("상품 조회 수가 올바르지 않습니다.");
        }

        this.date = date;
        this.productId = productId;
        this.likeCount = Objects.requireNonNullElse(likeCount, 0L);
        this.saleQuantity = Objects.requireNonNullElse(saleQuantity, 0L);
        this.viewCount = Objects.requireNonNullElse(viewCount, 0L);
    }

    public Metric plus(Metric other) {
        long likeCount = this.likeCount + other.likeCount;
        long saleQuantity = this.saleQuantity + other.saleQuantity;
        long viewCount = this.viewCount + other.viewCount;
        return new Metric(this.date, productId, likeCount, saleQuantity, viewCount);
    }

}
