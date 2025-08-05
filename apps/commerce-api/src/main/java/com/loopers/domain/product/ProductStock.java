package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.product.error.ProductErrorType;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "product_stocks")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductStock extends BaseEntity {

    /**
     * 아이디
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_stock_id", nullable = false, updatable = false)
    private Long id;

    /**
     * 상품 재고 수량
     */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    // -------------------------------------------------------------------------------------------------

    /**
     * 상품 옵션 아이디
     */
    @Column(name = "ref_product_option_id", nullable = false, updatable = false, unique = true)
    private Long productOptionId;

    // -------------------------------------------------------------------------------------------------

    @Builder
    private ProductStock(Integer quantity, Long productOptionId) {
        if (quantity == null || quantity < 0) {
            throw new BusinessException(CommonErrorType.INVALID, "상품 재고 수량은 0 이상이어야 합니다.");
        }

        if (productOptionId == null) {
            throw new BusinessException(CommonErrorType.INVALID, "상품 옵션 아이디가 올바르지 않습니다.");
        }

        this.quantity = quantity;
        this.productOptionId = productOptionId;
    }

    public void add(int amount) {
        if (amount <= 0) {
            throw new BusinessException(CommonErrorType.INVALID,
                    "0 이하의 값으로 상품 재고를 증가할 수 없습니다.");
        }

        this.quantity += amount;
    }

    public void deduct(int amount) {
        if (amount <= 0) {
            throw new BusinessException(CommonErrorType.INVALID,
                    "0 이하의 값으로 상품 재고를 차감할 수 없습니다.");
        }

        int deductedQuantity = this.quantity - amount;
        if (deductedQuantity < 0) {
            throw new BusinessException(ProductErrorType.NOT_ENOUGH);
        }

        this.quantity = deductedQuantity;
    }

}
