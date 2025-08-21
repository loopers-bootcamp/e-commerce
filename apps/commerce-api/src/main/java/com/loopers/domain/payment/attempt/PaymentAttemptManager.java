package com.loopers.domain.payment.attempt;

import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.attribute.AttemptStep;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentAttemptManager {

    private final PaymentRepository paymentRepository;

    @Transactional
    public void recordAsRequested(PaymentCommand.RecordAsRequested command) {
        PaymentAttempt requestedAttempt = PaymentAttempt.builder()
                .step(AttemptStep.REQUESTED)
                .orderId(command.getOrderId())
                .paymentId(command.getPaymentId())
                .build();
        paymentRepository.save(requestedAttempt);
    }

    @Transactional
    public void recordAsResponded(PaymentCommand.RecordAsResponded command) {
        PaymentAttempt respondedAttempt = PaymentAttempt.builder()
                .step(AttemptStep.RESPONDED)
                .transactionKey(command.getTransactionKey())
                .orderId(command.getOrderId())
                .paymentId(command.getPaymentId())
                .build();
        paymentRepository.save(respondedAttempt);
    }

    @Transactional
    public void recordAsSuccess(PaymentCommand.RecordAsSuccess command) {
        PaymentAttempt successAttempt = PaymentAttempt.builder()
                .step(AttemptStep.SUCCESS)
                .transactionKey(command.getTransactionKey())
                .orderId(command.getOrderId())
                .paymentId(command.getPaymentId())
                .build();
        paymentRepository.save(successAttempt);
    }

    @Transactional
    public void recordAsFailed(PaymentCommand.RecordAsFailed command) {
        PaymentAttempt failedAttempt = PaymentAttempt.builder()
                .step(AttemptStep.SUCCESS)
                .transactionKey(command.getTransactionKey())
                .failReason(command.getReason())
                .orderId(command.getOrderId())
                .paymentId(command.getPaymentId())
                .build();
        paymentRepository.save(failedAttempt);
    }

}
