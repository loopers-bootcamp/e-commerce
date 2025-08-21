package com.loopers.domain.payment.event;

import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentGateway;
import com.loopers.domain.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final PaymentService paymentService;
    private final PaymentGateway paymentGateway;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void requestTransaction(PaymentEvent.Ready event) {
        // Outbox
        PaymentCommand.RecordAsRequested requestCommand = PaymentCommand.RecordAsRequested.builder()
                .orderId(event.orderId())
                .paymentId(event.paymentId())
                .build();
        paymentService.recordAsRequested(requestCommand);

        PaymentGateway.RequestTransaction transaction = paymentGateway.requestTransaction(
                event.orderId(), event.cardType(), event.cardNumber(), event.amount());

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
