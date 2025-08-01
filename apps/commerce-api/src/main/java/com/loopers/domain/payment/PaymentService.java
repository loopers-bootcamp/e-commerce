package com.loopers.domain.payment;

import com.loopers.domain.payment.attribute.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public PaymentResult.Pay pay(PaymentCommand.Pay command) {
        Payment payment = Payment.builder()
                .amount(command.getAmount())
                .status(PaymentStatus.COMPLETE)
                .method(command.getPaymentMethod())
                .userId(command.getUserId())
                .orderId(command.getOrderId())
                .build();

        paymentRepository.save(payment);

        return PaymentResult.Pay.from(payment);
    }

}
