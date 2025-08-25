package com.loopers.application.payment;

import com.loopers.application.payment.processor.PaymentProcessContext;
import com.loopers.application.payment.processor.PaymentProcessor;
import com.loopers.domain.coupon.CouponCommand;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.order.OrderResult;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentResult;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.payment.attribute.PaymentStatus;
import com.loopers.domain.payment.error.PaymentErrorType;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserResult;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentFacade {

    private final PaymentService paymentService;
    private final UserService userService;
    private final OrderService orderService;
    private final ProductService productService;
    private final CouponService couponService;

    private final List<PaymentProcessor> processors;
    private final ApplicationEventPublisher eventPublisher;

    public PaymentOutput.Pay pay(PaymentInput.Pay input) {
        UserResult.GetUser user = userService.getUser(input.getUserName())
                .orElseThrow(() -> new BusinessException(CommonErrorType.UNAUTHENTICATED));

        UUID orderId = input.getOrderId();

        OrderCommand.GetOrderDetail orderCommand = OrderCommand.GetOrderDetail.builder()
                .orderId(orderId)
                .userId(user.getUserId())
                .build();
        OrderResult.GetOrderDetail order = orderService.getOrderDetail(orderCommand)
                .orElseThrow(() -> new BusinessException(CommonErrorType.NOT_FOUND));

        if (!order.getStatus().isPayable()) {
            throw new BusinessException(PaymentErrorType.UNPROCESSABLE);
        }

        PaymentProcessor paymentProcessor = processors.stream()
                .filter(processor -> processor.supports(input.getPaymentMethod()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(CommonErrorType.INTERNAL_ERROR, "결제를 처리할 수 없습니다."));

        PaymentProcessContext context = PaymentProcessContext.of(
                user.getUserId(),
                order,
                input.getCardType(),
                input.getCardNumber()
        );
        return paymentProcessor.process(context);
    }

    @Transactional
    public void conclude(PaymentInput.Conclude input) {
        UUID orderId = input.getOrderId();

        // Inbox 저장을 위해 가장 먼저 호출한다.
        PaymentCommand.Conclude concludeCommand = PaymentCommand.Conclude.builder()
                .transactionKey(input.getTransactionKey())
                .orderId(orderId)
                .amount(input.getAmount())
                .status(input.getStatus())
                .reason(input.getReason())
                .build();
        PaymentResult.Conclude payment = paymentService.conclude(concludeCommand);
        PaymentStatus paymentStatus = payment.getPaymentStatus();

        // 결제 상태가 확정되지 않았으면, 후속 작업을 진행하지 않는다.
        if (paymentStatus == PaymentStatus.READY) {
            return;
        }

        // 결제가 실패됐다면, 주문을 취소한다.
        if (paymentStatus == PaymentStatus.FAILED) {
            orderService.cancel(orderId);
            return;
        }

        OrderCommand.GetOrderDetail orderCommand = OrderCommand.GetOrderDetail.builder()
                .orderId(orderId)
                .build();
        OrderResult.GetOrderDetail order = orderService.getOrderDetail(orderCommand)
                .orElseThrow(() -> new BusinessException(CommonErrorType.NOT_FOUND));

        if (!order.getStatus().isPayable()) {
            throw new BusinessException(PaymentErrorType.UNPROCESSABLE);
        }

        List<ProductCommand.DeductStocks.Item> items = order.getProducts()
                .stream()
                .map(product -> ProductCommand.DeductStocks.Item.builder()
                        .productOptionId(product.getProductOptionId())
                        .amount(product.getQuantity())
                        .build()
                )
                .toList();

        ProductCommand.DeductStocks productCommand = ProductCommand.DeductStocks.builder().items(items).build();
        productService.deductStocks(productCommand);

        CouponCommand.Use couponCommand = CouponCommand.Use.builder()
                .userId(order.getUserId())
                .userCouponIds(order.getUserCouponIds())
                .build();
        couponService.use(couponCommand);

        orderService.complete(orderId);
    }

}
