package com.loopers.domain.activity;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자가 좋아요 한 상품
 */
@Getter
@Entity
@Table(
        name = "liked_product",
        indexes = {
                @Index(name = "idx__liked_product__ref_user_id", columnList = "userId"),
                @Index(name = "idx__liked_product__ref_product_id", columnList = "productId"),
        },
        uniqueConstraints = @UniqueConstraint(name = "uk__liked_product__ref_user_id__ref_product_id", columnNames = "userId, productId")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LikedProduct extends BaseEntity {

    /**
     * 아이디
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "liked_product_id", nullable = false, updatable = false)
    private Long id;

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
    private LikedProduct(Long userId, Long productId) {
        this.userId = userId;
        this.productId = productId;
    }

}
