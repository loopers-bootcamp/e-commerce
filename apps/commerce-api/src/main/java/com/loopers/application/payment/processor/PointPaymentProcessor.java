package com.loopers.application.payment.processor;

import com.loopers.application.payment.PaymentOutput;
import com.loopers.domain.coupon.CouponCommand;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentResult;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.payment.attribute.PaymentMethod;
import com.loopers.domain.point.PointCommand;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
class PointPaymentProcessor implements PaymentProcessor {

    private final PaymentService paymentService;
    private final OrderService orderService;
    private final ProductService productService;
    private final CouponService couponService;
    private final PointService pointService;

    @Override
    public boolean supports(PaymentMethod paymentMethod) {
        return paymentMethod == PaymentMethod.POINT;
    }

    @Transactional
    @Override
    public PaymentOutput.Ready process(PaymentProcessContext context) {
        PaymentCommand.Ready readyCommand = PaymentCommand.Ready.builder()
                .amount(context.paymentAmount())
                .paymentMethod(PaymentMethod.POINT)
                .cardType(null)
                .cardNumber(null)
                .userId(context.userId())
                .orderId(context.orderId())
                .build();
        PaymentResult.Ready paymentResult = paymentService.ready(readyCommand);

        List<ProductCommand.DeductStocks.Item> items = context.products()
                .stream()
                .map(product -> ProductCommand.DeductStocks.Item.builder()
                        .productOptionId(product.productOptionId())
                        .amount(product.quantity())
                        .build()
                )
                .toList();

        ProductCommand.DeductStocks productCommand = ProductCommand.DeductStocks.builder().items(items).build();
        productService.deductStocks(productCommand);

        Long userId = context.userId();
        CouponCommand.Use couponCommand = CouponCommand.Use.builder()
                .userId(userId)
                .userCouponIds(context.userCouponIds())
                .build();
        couponService.use(couponCommand);

        // 결제 금액이 0원이면 포인트를 차감할 필요가 없다.
        Long paymentAmount = context.paymentAmount();
        if (paymentAmount > 0) {
            PointCommand.Spend pointCommand = PointCommand.Spend.builder()
                    .amount(paymentAmount)
                    .userId(userId)
                    .build();
            pointService.spend(pointCommand);
        }

        UUID orderId = context.orderId();
//        PaymentCommand.Pay paymentCommand = PaymentCommand.Pay.builder()
//                .amount(paymentAmount)
//                .paymentMethod(PaymentMethod.POINT)
//                .userId(userId)
//                .orderId(orderId)
//                .build();
//        PaymentResult.Pay paymentResult = paymentService.pay(paymentCommand);

        orderService.complete(orderId);

        return PaymentOutput.Ready.from(paymentResult);
    }

}
