package com.loopers.domain.point;

import com.loopers.config.jpa.converter.CauseConverter;
import com.loopers.domain.BaseEntity;
import com.loopers.domain.point.attribute.Cause;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "point_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointHistory extends BaseEntity {

    /**
     * 아이디
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    /**
     * 원인
     */
    @Convert(converter = CauseConverter.class)
    @Column(name = "cause", nullable = false, updatable = false)
    private Cause cause;

    /**
     * 금액
     */
    @Column(name = "amount", nullable = false, updatable = false)
    private Long amount;

    // -------------------------------------------------------------------------------------------------

    /**
     * 사용자 아이디
     */
    @Column(name = "ref_user_id", nullable = false, updatable = false)
    private Long userId;

    // -------------------------------------------------------------------------------------------------

    @Builder
    private PointHistory(Cause cause, Long amount, Long userId) {
        if (cause == null) {
            throw new BusinessException(CommonErrorType.INVALID);
        }

        if (amount == null || amount <= 0) {
            throw new BusinessException(CommonErrorType.INVALID,
                    "금액은 양수여야 합니다.");
        }

        if (userId == null) {
            throw new BusinessException(CommonErrorType.INVALID);
        }

        this.cause = cause;
        this.amount = amount;
        this.userId = userId;
    }

}
