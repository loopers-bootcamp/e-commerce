package com.loopers.domain.point;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.point.error.PointErrorType;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "points")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Point extends BaseEntity {

    /**
     * 아이디
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "point_id", nullable = false, updatable = false)
    private Long id;

    /**
     * 잔액
     */
    @Column(name = "balance", nullable = false)
    private Long balance;

    // -------------------------------------------------------------------------------------------------

    /**
     * 사용자 아이디
     */
    @Column(name = "ref_user_id", nullable = false, unique = true)
    private Long userId;

    // -------------------------------------------------------------------------------------------------

    private static final long MAX_BALANCE = 100_000_000L;

    @Builder
    private Point(Long balance, Long userId) {
        if (balance == null || balance < 0) {
            throw new BusinessException(CommonErrorType.INVALID,
                    "포인트는 0 이상의 값이어야 합니다.");
        }

        if (balance > MAX_BALANCE) {
            throw new BusinessException(PointErrorType.EXCESSIVE);
        }

        if (userId == null) {
            throw new BusinessException(CommonErrorType.INVALID);
        }

        this.balance = balance;
        this.userId = userId;
    }

    public void charge(long amount) {
        if (amount <= 0) {
            throw new BusinessException(CommonErrorType.INVALID,
                    "0 이하의 값으로 포인트를 충전할 수 없습니다.");
        }

        long increaseBalance = this.balance + amount;
        if (increaseBalance > MAX_BALANCE) {
            throw new BusinessException(PointErrorType.EXCESSIVE);
        }

        this.balance = increaseBalance;
    }

    public void spend(long amount) {
        if (amount <= 0) {
            throw new BusinessException(CommonErrorType.INVALID,
                    "0 이하의 값으로 포인트를 차감할 수 없습니다.");
        }

        long decreasedBalance = this.balance - amount;
        if (decreasedBalance < 0) {
            throw new BusinessException(PointErrorType.NOT_ENOUGH);
        }

        this.balance = decreasedBalance;
    }

}
