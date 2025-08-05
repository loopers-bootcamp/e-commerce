package com.loopers.domain.coupon;

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
@Table(name = "coupon_stocks")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponStock extends BaseEntity {

    /**
     * 아이디
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_stock_id", nullable = false, updatable = false)
    private Long id;

    /**
     * 쿠폰 재고 수량
     */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    // -------------------------------------------------------------------------------------------------

    /**
     * 쿠폰 아이디
     */
    @Column(name = "ref_coupon_id", nullable = false, updatable = false, unique = true)
    private Long couponId;

    // -------------------------------------------------------------------------------------------------

    @Builder
    private CouponStock(Integer quantity, Long couponId) {
        if (quantity == null || quantity < 0) {
            throw new BusinessException(CommonErrorType.INVALID, "쿠폰 재고 수량은 0 이상이어야 합니다.");
        }

        if (couponId == null) {
            throw new BusinessException(CommonErrorType.INVALID, "쿠폰 아이디가 올바르지 않습니다.");
        }

        this.quantity = quantity;
        this.couponId = couponId;
    }

    public void add(int amount) {
        if (amount <= 0) {
            throw new BusinessException(CommonErrorType.INVALID,
                    "0 이하의 값으로 쿠폰 재고를 증가할 수 없습니다.");
        }

        this.quantity += amount;
    }

    public void deduct(int amount) {
        if (amount <= 0) {
            throw new BusinessException(CommonErrorType.INVALID,
                    "0 이하의 값으로 쿠폰 재고를 차감할 수 없습니다.");
        }

        int deductedQuantity = this.quantity - amount;
        if (deductedQuantity < 0) {
            throw new BusinessException(ProductErrorType.NOT_ENOUGH);
        }

        this.quantity = deductedQuantity;
    }

}
