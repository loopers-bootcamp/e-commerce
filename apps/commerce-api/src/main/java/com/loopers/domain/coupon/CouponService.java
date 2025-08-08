package com.loopers.domain.coupon;

import com.loopers.annotation.ReadOnlyTransactional;
import com.loopers.domain.coupon.attribute.DiscountPolicy;
import com.loopers.domain.coupon.error.CouponErrorType;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    @ReadOnlyTransactional
    public int getDiscountAmount(CouponCommand.GetDiscountAmount command) {
        List<Long> userCouponIds = command.getUserCouponIds();
        if (CollectionUtils.isEmpty(userCouponIds)) {
            return 0;
        }

        List<Pair<Coupon, UserCoupon>> pairs = couponRepository.findUserCoupons(userCouponIds);
        if (pairs.size() != userCouponIds.size()) {
            throw new BusinessException(CommonErrorType.NOT_FOUND);
        }

        int discountAmount = 0;
        int maxDiscountAmount = command.getTotalPrice().intValue();

        for (Pair<Coupon, UserCoupon> pair : pairs) {
            Coupon coupon = pair.getFirst();
            UserCoupon userCoupon = pair.getSecond();

            if (!userCoupon.isUsable()) {
                throw new BusinessException(CouponErrorType.UNAVAILABLE);
            }

            DiscountPolicy policy = coupon.getDiscountPolicy();
            discountAmount += policy.calculateDiscountAmount(command.getTotalPrice());

            // 복수의 쿠폰을 사용하면, 최대 할인 가능 금액은 최소값으로 적용한다.
            maxDiscountAmount = Math.min(maxDiscountAmount, policy.getMaxDiscountAmount());
        }

        return Math.min(discountAmount, maxDiscountAmount);
    }

    @Transactional
    public void use(CouponCommand.Use command) {
        List<Long> userCouponIds = command.getUserCouponIds();
        if (CollectionUtils.isEmpty(userCouponIds)) {
            return;
        }

        List<Pair<Coupon, UserCoupon>> pairs = couponRepository.findUserCoupons(userCouponIds)
                .stream()
                .filter(pair -> Objects.equals(pair.getSecond().getUserId(), command.getUserId()))
                .toList();
        if (pairs.size() != userCouponIds.size()) {
            throw new BusinessException(CommonErrorType.NOT_FOUND);
        }

        List<UserCoupon> usedCoupons = pairs.stream()
                .map(Pair::getSecond)
                .peek(UserCoupon::use)
                .toList();
        couponRepository.saveUserCoupons(usedCoupons);
    }

    @Transactional
    public void revoke(Long couponId) {
        Coupon coupon = couponRepository.findCoupon(couponId)
                .orElseThrow(() -> new BusinessException(CommonErrorType.NOT_FOUND, "쿠폰을 찾을 수 없습니다."));

        coupon.revoke();
        couponRepository.saveCoupon(coupon);
    }

    @Transactional
    public void addStocks(CouponCommand.AddStocks command) {
        Long couponId = command.getCouponId();
        Coupon coupon = couponRepository.findCoupon(couponId)
                .orElseThrow(() -> new BusinessException(CommonErrorType.NOT_FOUND, "쿠폰을 찾을 수 없습니다."));

        // X-Lock을 걸기 전에 쿠폰이 폐기되었는지 확인한다.
        if (coupon.isRevoked()) {
            throw new BusinessException(CouponErrorType.REVOKED);
        }

        if (!coupon.getIssuedRange().contains(Instant.now())) {
            throw new BusinessException(CouponErrorType.NOT_ISSUABLE);
        }

        CouponStock stock = couponRepository.findStockForUpdate(coupon.getId())
                .orElseThrow(() -> new BusinessException(CommonErrorType.NOT_FOUND, "쿠폰 재고를 찾을 수 없습니다."));

        stock.add(command.getAmount());
        couponRepository.saveStock(stock);
    }

    @Transactional
    public void deductStocks(CouponCommand.DeductStocks command) {
        Long couponId = command.getCouponId();
        Coupon coupon = couponRepository.findCoupon(couponId)
                .orElseThrow(() -> new BusinessException(CommonErrorType.NOT_FOUND, "쿠폰을 찾을 수 없습니다."));

        // X-Lock을 걸기 전에 쿠폰이 폐기되었는지 확인한다.
        if (coupon.isRevoked()) {
            throw new BusinessException(CouponErrorType.REVOKED);
        }

        if (!coupon.getIssuedRange().contains(Instant.now())) {
            throw new BusinessException(CouponErrorType.NOT_ISSUABLE);
        }

        CouponStock stock = couponRepository.findStockForUpdate(coupon.getId())
                .orElseThrow(() -> new BusinessException(CommonErrorType.NOT_FOUND, "쿠폰 재고를 찾을 수 없습니다."));

        stock.deduct(command.getAmount());
        couponRepository.saveStock(stock);
    }

}
