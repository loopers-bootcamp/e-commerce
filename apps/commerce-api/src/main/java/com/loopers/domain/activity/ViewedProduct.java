package com.loopers.domain.activity;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자가 조회한 상품
 */
@Getter
@Entity
@Table(
        name = "viewed_products",
        indexes = {
                @Index(columnList = "ref_user_id"),
                @Index(columnList = "ref_product_id"),
        },
        uniqueConstraints = @UniqueConstraint(columnNames = {"ref_user_id", "ref_product_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ViewedProduct extends BaseEntity {

    /**
     * 아이디
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "viewed_product_id", nullable = false, updatable = false)
    private Long id;

    /**
     * 상품 조회 수
     */
    @Column(name = "view_count", nullable = false)
    private Long viewCount;

    // -------------------------------------------------------------------------------------------------

    /**
     * 사용자 아이디
     */
    @Column(name = "ref_user_id", nullable = false, updatable = false)
    private Long userId;

    /**
     * 상품 아이디
     */
    @Column(name = "ref_product_id", nullable = false, updatable = false)
    private Long productId;

    // -------------------------------------------------------------------------------------------------

    @Builder
    private ViewedProduct(Long viewCount, Long userId, Long productId) {
        if (viewCount == null || viewCount < 0) {
            throw new BusinessException(CommonErrorType.INVALID, "상품 조회 수는 0 이상이어야 합니다.");
        }

        if (userId == null) {
            throw new BusinessException(CommonErrorType.INVALID, "사용자 아이디가 올바르지 않습니다.");
        }

        if (productId == null) {
            throw new BusinessException(CommonErrorType.INVALID, "상품 아이디가 올바르지 않습니다.");
        }

        this.viewCount = viewCount;
        this.userId = userId;
        this.productId = productId;
    }

    public void view() {
        // 오버플로우를 방지한다.
        if (this.viewCount < Long.MAX_VALUE) {
            this.viewCount++;
        }
    }

}
