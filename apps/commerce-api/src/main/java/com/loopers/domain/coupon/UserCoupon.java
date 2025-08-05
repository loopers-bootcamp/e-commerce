package com.loopers.domain.coupon;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.attribute.TimeRange;
import com.loopers.domain.coupon.error.CouponErrorType;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Entity
@Table(
        name = "user_coupons",
        indexes = @Index(columnList = "ref_user_id"),
        uniqueConstraints = @UniqueConstraint(columnNames = {"ref_user_id", "ref_coupon_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCoupon extends BaseEntity {

    /**
     * 아이디
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_coupon_id", nullable = false, updatable = false)
    private Long id;

    /**
     * 쿠폰 유효 기간
     */
    @Embedded
    @AttributeOverride(name = "start", column = @Column(name = "coupon_valid_started_at"))
    @AttributeOverride(name = "end", column = @Column(name = "coupon_valid_ended_at"))
    private TimeRange validRange;

    /**
     * 쿠폰 사용 여부
     */
    @Column(name = "coupon_used", nullable = false)
    private Boolean used;

    // -------------------------------------------------------------------------------------------------

    /**
     * 사용자 아이디
     */
    @Column(name = "ref_user_id", nullable = false, updatable = false)
    private Long userId;

    /**
     * 쿠폰 아이디
     */
    @Column(name = "ref_coupon_id", nullable = false, updatable = false)
    private Long couponId;

    // -------------------------------------------------------------------------------------------------

    @Builder
    private UserCoupon(TimeRange validRange, Long userId, Long couponId) {
        if (validRange == null) {
            throw new BusinessException(CommonErrorType.INVALID, "쿠폰 유효 기간이 올바르지 않습니다.");
        }

        if (userId == null) {
            throw new BusinessException(CommonErrorType.INVALID, "사용자 아이디가 올바르지 않습니다.");
        }

        if (couponId == null) {
            throw new BusinessException(CommonErrorType.INVALID, "쿠폰 아이디가 올바르지 않습니다.");
        }

        this.validRange = validRange;
        this.used = false;
        this.userId = userId;
        this.couponId = couponId;
    }

    public void use() {
        if (this.used) {
            return;
        }

        if (!this.validRange.contains(Instant.now())) {
            throw new BusinessException(CouponErrorType.UNAVAILABLE);
        }

        this.used = true;
    }

}
