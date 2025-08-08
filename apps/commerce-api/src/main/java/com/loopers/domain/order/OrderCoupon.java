package com.loopers.domain.order;

import com.loopers.domain.BaseEntity;
import com.loopers.support.AddedItem;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Entity
@Table(name = "order_coupons", indexes = {
        @Index(columnList = "ref_order_id"),
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderCoupon extends BaseEntity implements AddedItem<Long, UUID> {

    /**
     * 아이디
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_coupon_id", nullable = false, updatable = false)
    private Long id;

    // -------------------------------------------------------------------------------------------------

    /**
     * 주문 아이디
     */
    @Column(name = "ref_order_id", nullable = false, updatable = false)
    private UUID orderId;

    /**
     * 사용자 쿠폰 아이디
     */
    @Column(name = "ref_user_coupon_id", nullable = false, updatable = false, unique = true)
    private Long userCouponId;

    // -------------------------------------------------------------------------------------------------

    @Builder
    private OrderCoupon(UUID orderId, Long userCouponId) {
        if (orderId == null) {
            throw new BusinessException(CommonErrorType.INVALID, "주문 아이디가 올바르지 않습니다.");
        }

        if (userCouponId == null) {
            throw new BusinessException(CommonErrorType.INVALID, "사용자 쿠폰 아이디가 올바르지 않습니다.");
        }

        this.orderId = orderId;
        this.userCouponId = userCouponId;
    }

    @Override
    public UUID getParentId() {
        return this.orderId;
    }

}
