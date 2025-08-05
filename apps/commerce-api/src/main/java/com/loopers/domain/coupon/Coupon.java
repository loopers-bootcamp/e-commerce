package com.loopers.domain.coupon;

import com.loopers.config.jpa.converter.PeriodConverter;
import com.loopers.domain.BaseEntity;
import com.loopers.domain.attribute.TimeRange;
import com.loopers.domain.coupon.attribute.DiscountPolicy;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.time.Period;
import java.time.ZonedDateTime;

@Getter
@Entity
@Table(name = "coupons")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon extends BaseEntity {

    /**
     * 아이디
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_id", nullable = false, updatable = false)
    private Long id;

    /**
     * 쿠폰 이름
     */
    @Column(name = "coupon_name", nullable = false)
    private String name;

    /**
     * 할인 정책
     */
    @Embedded
    private DiscountPolicy discountPolicy;

    /**
     * 쿠폰 유효 기간
     */
    @Convert(converter = PeriodConverter.class)
    @Column(name = "coupon_validity_period", nullable = false)
    private Period validityPeriod;

    /**
     * 쿠폰 발급 기간
     */
    @Embedded
    @AttributeOverride(name = "start", column = @Column(name = "coupon_issue_started_at"))
    @AttributeOverride(name = "end", column = @Column(name = "coupon_issue_ended_at"))
    private TimeRange issuedRange;

    /**
     * 폐기일시
     */
    @Column(name = "revoked_at")
    private ZonedDateTime revokedAt;

    // -------------------------------------------------------------------------------------------------

    @Builder
    private Coupon(
            String name,
            DiscountPolicy discountPolicy,
            Period validityPeriod,
            TimeRange issuedRange,
            ZonedDateTime revokedAt
    ) {
        if (!StringUtils.hasText(name)) {
            throw new BusinessException(CommonErrorType.INVALID, "이름이 올바르지 않습니다.");
        }

        if (discountPolicy == null) {
            throw new BusinessException(CommonErrorType.INVALID, "할인 정책이 올바르지 않습니다.");
        }

        if (validityPeriod == null || validityPeriod.isNegative() || validityPeriod.isZero()) {
            throw new BusinessException(CommonErrorType.INVALID, "쿠폰 유효 기간이 올바르지 않습니다.");
        }

        if (issuedRange == null) {
            throw new BusinessException(CommonErrorType.INVALID, "쿠폰 발급 기간이 올바르지 않습니다.");
        }

        if (revokedAt == null) {
            throw new BusinessException(CommonErrorType.INVALID, "폐기일시가 올바르지 않습니다.");
        }

        this.name = name;
        this.discountPolicy = discountPolicy;
        this.validityPeriod = validityPeriod;
        this.issuedRange = issuedRange;
        this.revokedAt = revokedAt;
    }

    public void revoke() {
        if (this.revokedAt == null) {
            this.revokedAt = ZonedDateTime.now();
        }
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

}
