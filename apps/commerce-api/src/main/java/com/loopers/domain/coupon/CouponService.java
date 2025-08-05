package com.loopers.domain.coupon;

import com.loopers.domain.coupon.error.CouponErrorType;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

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

        CouponStock stock = couponRepository.findStockForUpdate(coupon.getId())
                .orElseThrow(() -> new BusinessException(CommonErrorType.NOT_FOUND, "쿠폰 재고를 찾을 수 없습니다."));

        stock.deduct(command.getAmount());
        couponRepository.saveStock(stock);
    }

}
