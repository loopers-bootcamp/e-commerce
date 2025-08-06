package com.loopers.application.order;

import com.loopers.domain.coupon.CouponCommand;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.order.OrderResult;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentResult;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.point.PointResult;
import com.loopers.domain.point.PointService;
import com.loopers.domain.point.error.PointErrorType;
import com.loopers.domain.product.ProductResult;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.error.ProductErrorType;
import com.loopers.domain.user.UserResult;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderFacade {

    private final OrderService orderService;
    private final UserService userService;
    private final ProductService productService;
    private final CouponService couponService;
    private final PointService pointService;
    private final PaymentService paymentService;

    public OrderOutput.GetOrderDetail getOrderDetail(OrderInput.GetOrderDetail input) {
        UserResult.GetUser user = userService.getUser(input.getUserName())
                .orElseThrow(() -> new BusinessException(CommonErrorType.UNAUTHENTICATED));

        OrderCommand.GetOrderDetail orderCommand = OrderCommand.GetOrderDetail.builder()
                .orderId(input.getOrderId())
                .userId(user.getUserId())
                .build();
        OrderResult.GetOrderDetail order = orderService.getOrderDetail(orderCommand)
                .orElseThrow(() -> new BusinessException(CommonErrorType.NOT_FOUND));

        PaymentCommand.GetPayment paymentCommand = PaymentCommand.GetPayment.builder()
                .orderId(input.getOrderId())
                .userId(user.getUserId())
                .build();
        PaymentResult.GetPayment payment = paymentService.getPayment(paymentCommand)
                .orElseThrow(() -> new BusinessException(CommonErrorType.NOT_FOUND));

        return OrderOutput.GetOrderDetail.from(order, payment);
    }

    public OrderOutput.Create create(OrderInput.Create input) {
        List<OrderInput.Create.Product> products = input.getProducts();
        if (CollectionUtils.isEmpty(products)) {
            throw new BusinessException(CommonErrorType.INVALID, "주문할 상품이 없습니다.");
        }

        UserResult.GetUser user = userService.getUser(input.getUserName())
                .orElseThrow(() -> new BusinessException(CommonErrorType.UNAUTHENTICATED));

        List<Long> productOptionIds = products.stream().map(OrderInput.Create.Product::getProductOptionId).toList();
        ProductResult.GetProductOptions options = productService.getProductOptions(productOptionIds)
                .orElseThrow(() -> new BusinessException(CommonErrorType.NOT_FOUND));

        OrderCart cart = OrderCart.from(input, options);
        if (!cart.isEnoughStock()) {
            throw new BusinessException(ProductErrorType.NOT_ENOUGH);
        }

        CouponCommand.GetDiscountAmount couponCommand = CouponCommand.GetDiscountAmount.builder()
                .totalPrice(cart.getTotalPrice())
                .userCouponIds(input.getUserCouponIds())
                .build();
        int discountAmount = couponService.getDiscountAmount(couponCommand);

        Long userId = user.getUserId();
        PointResult.GetPoint point = pointService.getPoint(userId)
                .orElseThrow(() -> new BusinessException(CommonErrorType.NOT_FOUND));
        long balanceWithIncreasedPurchasingPower = point.getBalance() + discountAmount;
        if (!cart.isEnoughPoint(balanceWithIncreasedPurchasingPower)) {
            throw new BusinessException(PointErrorType.NOT_ENOUGH);
        }

        // 후차감: 주문 시 재화 검증만 하고, 결제 시 비로소 재화를 차감한다.
        OrderResult.Create order = orderService.create(cart.toCommand(userId, discountAmount));

        return OrderOutput.Create.from(order);
    }

}
