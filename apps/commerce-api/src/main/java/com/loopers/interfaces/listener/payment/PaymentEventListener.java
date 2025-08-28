package com.loopers.interfaces.listener.payment;

import com.loopers.application.payment.PaymentFacade;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.payment.event.PaymentEvent;
import com.loopers.domain.payment.event.PaymentGatewayEvent;
import com.loopers.support.annotation.Inboxing;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final PaymentFacade paymentFacade;
    private final PaymentService paymentService;
    private final OrderService orderService;

    @Async
    @Inboxing(idempotent = true)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void pay(PaymentEvent.Ready event) {
        paymentFacade.pay(event.paymentId());
    }

    /**
     * {@link TransactionPhase#BEFORE_COMMIT}: 결제 성공과 주문 완료의 강력한 일관성을 보장해야 한다.
     */
    @Inboxing(idempotent = true)
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void completeOrder(PaymentEvent.Paid event) {
        orderService.complete(event.orderId());
    }

    /**
     * {@link TransactionPhase#BEFORE_COMMIT}: 결제 실패와 주문 취소의 강력한 일관성을 보장해야 한다.
     */
    @Inboxing(idempotent = true)
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void cancelOrder(PaymentEvent.Failed event) {
        orderService.cancel(event.orderId());
    }

    /**
     * {@link Async}: 메인 트랜잭션이 동기적으로 기다려야 할 필요 없음.
     * <p>
     * {@link EventListener}: 메인 트랜잭션이 롤백돼도, PG에서 받은 결과를 보관해야 한다.
     */
    @Async
    @EventListener
    public void recordTransactionAsSuccess(PaymentGatewayEvent.Success event) {
        // Inbox
        PaymentCommand.RecordAsSuccess successCommand = PaymentCommand.RecordAsSuccess.builder()
                .transactionKey(event.transactionKey())
                .orderId(event.orderId())
                .paymentId(event.paymentId())
                .build();
        paymentService.recordAsSuccess(successCommand);
    }

    /**
     * {@link Async}: 메인 트랜잭션이 동기적으로 기다려야 할 필요 없음.
     * <p>
     * {@link EventListener}: 메인 트랜잭션이 롤백돼도, PG에서 받은 결과를 보관해야 한다.
     */
    @Async
    @EventListener
    public void recordTransactionAsFailed(PaymentGatewayEvent.Failed event) {
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
