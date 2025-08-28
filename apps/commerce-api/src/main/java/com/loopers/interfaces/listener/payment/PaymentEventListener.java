package com.loopers.interfaces.listener.payment;

import com.loopers.domain.coupon.CouponCommand;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.order.OrderResult;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentGateway;
import com.loopers.domain.payment.PaymentResult;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.payment.attribute.PaymentMethod;
import com.loopers.domain.payment.error.PaymentErrorType;
import com.loopers.domain.payment.event.PaymentEvent;
import com.loopers.domain.point.PointCommand;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductService;
import com.loopers.support.annotation.Inboxing;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final PaymentService paymentService;
    private final OrderService orderService;
    private final ProductService productService;
    private final CouponService couponService;
    private final PointService pointService;

    private final PaymentGateway paymentGateway;

    @Async
    @Inboxing(idempotent = true)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReadyPayment(PaymentEvent.Ready event) {
        OrderCommand.GetOrderDetail orderCommand = OrderCommand.GetOrderDetail.builder()
                .orderId(event.orderId())
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

        Long userId = order.getUserId();
        CouponCommand.Use couponCommand = CouponCommand.Use.builder()
                .userId(userId)
                .userCouponIds(order.getUserCouponIds())
                .build();
        couponService.use(couponCommand);

        // 결제 금액이 0원이면 포인트를 차감할 필요가 없다.
        Long paymentAmount = event.amount();
        if (paymentAmount > 0) {
            PointCommand.Spend pointCommand = PointCommand.Spend.builder()
                    .amount(paymentAmount)
                    .userId(userId)
                    .build();
            pointService.spend(pointCommand);
        }

        PaymentCommand.Pay paymentCommand = PaymentCommand.Pay.builder()
                .amount(paymentAmount)
                .paymentMethod(PaymentMethod.POINT)
                .userId(userId)
                .orderId(event.orderId())
                .build();
        PaymentResult.Pay paymentResult = paymentService.pay(paymentCommand);

        // ---

        // Outbox
        PaymentCommand.RecordAsRequested requestCommand = PaymentCommand.RecordAsRequested.builder()
                .orderId(event.orderId())
                .paymentId(event.paymentId())
                .build();
        paymentService.recordAsRequested(requestCommand);

        PaymentGateway.Request.Transact request = new PaymentGateway.Request.Transact(
                event.orderId(),
                event.cardType(),
                event.cardNumber(),
                event.amount()
        );

        PaymentGateway.Response.Transact transaction = paymentGateway.transact(request);

        // TODO: outbox 이벤트로 빼자!
        // Inbox
        PaymentCommand.RecordAsResponded respondCommand = PaymentCommand.RecordAsResponded.builder()
                .transactionKey(transaction.transactionKey())
                .orderId(event.orderId())
                .paymentId(event.paymentId())
                .build();
        paymentService.recordAsResponded(respondCommand);
    }

    /**
     * 트랜잭션 결과와 상관없이 실행한다.
     */
    @Async
    @EventListener
    public void recordTransactionAsSuccess(PaymentEvent.Success event) {
        // Inbox
        PaymentCommand.RecordAsSuccess successCommand = PaymentCommand.RecordAsSuccess.builder()
                .transactionKey(event.transactionKey())
                .orderId(event.orderId())
                .paymentId(event.paymentId())
                .build();
        paymentService.recordAsSuccess(successCommand);
    }

    /**
     * 트랜잭션 결과와 상관없이 실행한다.
     */
    @Async
    @EventListener
    public void recordTransactionAsFailed(PaymentEvent.Failed event) {
        // Inbox
        PaymentCommand.RecordAsFailed failedCommand = PaymentCommand.RecordAsFailed.builder()
                .transactionKey(event.transactionKey())
                .reason(event.reason())
                .orderId(event.orderId())
                .paymentId(event.paymentId())
                .build();
        paymentService.recordAsFailed(failedCommand);
    }

}
