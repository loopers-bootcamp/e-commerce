package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
import com.loopers.support.ItemAdder;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

@Getter
@Entity
@Table(
        name = "products",
        indexes = {
                @Index(name = "products_idx_latest", columnList = "created_at desc, product_id desc"),
                @Index(name = "products_idx_latest_with_brand", columnList = "ref_brand_id, created_at desc, product_id desc"),
                @Index(name = "products_idx_popular", columnList = "like_count desc, product_id desc"),
                @Index(name = "products_idx_popular_with_brand", columnList = "ref_brand_id, like_count desc, product_id desc"),
                @Index(name = "products_idx_cheap", columnList = "base_price, product_id desc"),
                @Index(name = "products_idx_cheap_with_brand", columnList = "ref_brand_id, base_price, product_id desc"),
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    /**
     * 아이디
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id", nullable = false, updatable = false)
    private Long id;

    /**
     * 상품 이름
     */
    @Column(name = "product_name", nullable = false)
    private String name;

    /**
     * 기본 가격
     */
    @Column(name = "base_price", nullable = false)
    private Integer basePrice;

    /**
     * 상품 좋아요 수
     */
    @Column(name = "like_count", nullable = false)
    private Long likeCount;

    // -------------------------------------------------------------------------------------------------

    /**
     * 브랜드 아이디
     */
    @Column(name = "ref_brand_id")
    private Long brandId;

    // -------------------------------------------------------------------------------------------------

    /**
     * 옵션 목록
     */
    @Transient
    private List<ProductOption> options = Collections.emptyList();

    // -------------------------------------------------------------------------------------------------

    @Builder
    private Product(String name, Integer basePrice, Long brandId) {
        if (!StringUtils.hasText(name)) {
            throw new BusinessException(CommonErrorType.INVALID, "이름이 올바르지 않습니다.");
        }

        if (basePrice == null || basePrice < 0) {
            throw new BusinessException(CommonErrorType.INVALID, "기본 가격은 0 이상이어야 합니다.");
        }

        this.name = name;
        this.basePrice = basePrice;
        this.brandId = brandId;
        this.likeCount = 0L;
    }

    public void like() {
        // 오버플로우를 방지한다.
        if (this.likeCount < Long.MAX_VALUE) {
            this.likeCount++;
        }
    }

    public void dislike() {
        // 언더플로우를 방지한다.
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    public void addOptions(List<ProductOption> options) {
        this.options = ItemAdder.addItemsTo(this.id, this.options, options, false);
    }

}
