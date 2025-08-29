package com.loopers.application.payment.processor;

import com.loopers.application.payment.PaymentOutput;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentGateway;
import com.loopers.domain.payment.PaymentResult;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.payment.attribute.PaymentMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
class CardPaymentProcessor implements PaymentProcessor {

    private final PaymentService paymentService;
    private final PaymentGateway paymentGateway;

    @Qualifier(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME)
    private final TaskExecutor taskExecutor;

    @Override
    public boolean supports(PaymentMethod paymentMethod) {
        return paymentMethod == PaymentMethod.CARD;
    }

    @Transactional
    @Override
    public PaymentOutput.Pay process(PaymentProcessContext context) {
        // Outbox
        paymentService.recordAsRequested(
                PaymentCommand.RecordAsRequested.builder()
                        .orderId(context.orderId())
                        .paymentId(context.paymentId())
                        .build()
        );

        // 메인 트랜잭션에 영향을 주지 않기 위해, 비동기로 수행한다.
        taskExecutor.execute(() -> {
            PaymentGateway.Request.Transact request = new PaymentGateway.Request.Transact(
                    context.orderId(),
                    context.cardType(),
                    context.cardNumber(),
                    context.paymentAmount()
            );
            PaymentGateway.Response.Transact transaction = paymentGateway.transact(request);

            // Inbox
            paymentService.recordAsResponded(
                    PaymentCommand.RecordAsResponded.builder()
                            .transactionKey(transaction.transactionKey())
                            .orderId(context.orderId())
                            .paymentId(context.paymentId())
                            .build()
            );
        });

        PaymentResult.Pending payment = paymentService.pending(context.paymentId());

        return PaymentOutput.Pay.from(payment);
    }

}
