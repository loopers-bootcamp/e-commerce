package com.loopers.domain.activity;

import com.loopers.domain.BaseEntity;
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
        this.viewCount = viewCount;
        this.userId = userId;
        this.productId = productId;
    }

    public void view() {
        this.viewCount = this.viewCount == null ? 1L : this.viewCount + 1L;
    }

}
