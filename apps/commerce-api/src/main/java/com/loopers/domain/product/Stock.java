package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.product.error.ProductErrorType;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "stock")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stock extends BaseEntity {

    /**
     * 아이디
     */
    @Id
    @Column(name = "stock_id", nullable = false, updatable = false)
    private Long id;

    /**
     * 재고 수량
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
    private Stock(Integer quantity, Long productOptionId) {
        this.quantity = quantity;
        this.productOptionId = productOptionId;
    }

    public void add(int amount) {
        if (amount <= 0) {
            throw new BusinessException(CommonErrorType.INVALID,
                    "0 이하의 값으로 재고를 증가할 수 없습니다.");
        }

        this.quantity += amount;
    }

    public void deduct(int amount) {
        if (amount <= 0) {
            throw new BusinessException(CommonErrorType.INVALID,
                    "0 이하의 값으로 재고를 차감할 수 없습니다.");
        }

        int deductedQuantity = this.quantity - amount;
        if (deductedQuantity < 0) {
            throw new BusinessException(ProductErrorType.NOT_ENOUGH);
        }

        this.quantity = deductedQuantity;
    }

}
