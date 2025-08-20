package com.loopers.domain.payment.attempt;

import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.PaymentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentAttemptManager {

    private final PaymentRepository paymentRepository;

    @Transactional
    public PaymentResult.RecordRequest recordRequest(Long paymentId) {
        PaymentAttempt requestedAttempt = PaymentAttempt.request(paymentId);
        paymentRepository.save(requestedAttempt);

        return PaymentResult.RecordRequest.from(requestedAttempt);
    }

    @Transactional
    public PaymentResult.RecordRequest recordRequest(PaymentCommand.RecordResponse command) {
        PaymentAttempt requestedAttempt = PaymentAttempt.respond(
                command.getMerchantUid(),
                command.getTransactionKey(),
                command.getPaymentId()
        );
        paymentRepository.save(requestedAttempt);

        return PaymentResult.RecordRequest.from(requestedAttempt);
    }

}
