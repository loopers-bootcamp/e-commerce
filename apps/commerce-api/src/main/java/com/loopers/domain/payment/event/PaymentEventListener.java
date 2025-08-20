package com.loopers.domain.payment.event;

import com.loopers.domain.payment.PaymentGateway;
import com.loopers.domain.payment.PaymentService;
import com.loopers.infrastructure.payment.client.PgSimulatorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final PaymentService paymentService;
    private final PaymentGateway paymentGateway;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void requestTransaction(PaymentEvent.Ready event) {
        PgSimulatorResponse.RequestTransaction transaction = paymentGateway.requestTransaction(
                event.orderId(), event.cardType(), event.cardNumber(), event.amount());
        paymentService.recordRequest(event.paymentId());
    }

}
