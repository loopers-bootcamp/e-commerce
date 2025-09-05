package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
import com.loopers.support.AddedItem;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@Getter
@Entity
@Table(
        name = "product_options",
        indexes = @Index(columnList = "ref_product_id")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductOption extends BaseEntity implements AddedItem<Long, Long> {

    /**
     * 아이디
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_option_id", nullable = false, updatable = false)
    private Long id;

    /**
     * 상품 옵션 이름
     */
    @Column(name = "product_option_name", nullable = false)
    private String name;

    /**
     * 추가 가격
     */
    @Column(name = "additional_price", nullable = false)
    private Integer additionalPrice;

    // -------------------------------------------------------------------------------------------------

    /**
     * 상품 아이디
     */
    @Column(name = "ref_product_id", nullable = false)
    private Long productId;

    // -------------------------------------------------------------------------------------------------

    @Builder
    private ProductOption(String name, Integer additionalPrice, Long productId) {
        if (!StringUtils.hasText(name)) {
            throw new BusinessException(CommonErrorType.INVALID, "이름이 올바르지 않습니다.");
        }

        if (additionalPrice == null || additionalPrice < 0) {
            throw new BusinessException(CommonErrorType.INVALID, "추가 가격은 0 이상이어야 합니다.");
        }

        if (productId == null) {
            throw new BusinessException(CommonErrorType.INVALID, "상품 아이디가 올바르지 않습니다.");
        }

        this.name = name;
        this.additionalPrice = additionalPrice;
        this.productId = productId;
    }

    @Override
    public Long getParentId() {
        return this.productId;
    }

}
