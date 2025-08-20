package com.loopers.domain.payment;

import com.loopers.annotation.ReadOnlyTransactional;
import com.loopers.domain.payment.attempt.PaymentAttemptManager;
import com.loopers.domain.payment.attribute.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Delegate
    private final PaymentAttemptManager paymentAttemptManager;

    @ReadOnlyTransactional
    public Optional<PaymentResult.GetPayment> getPayment(PaymentCommand.GetPayment command) {
        return paymentRepository.findPayment(command.getOrderId())
                .filter(payment -> Objects.equals(payment.getUserId(), command.getUserId()))
                .map(PaymentResult.GetPayment::from);
    }

    @Transactional
    public PaymentResult.Pay ready(PaymentCommand.Ready command) {
        Payment payment = Payment.builder()
                .amount(command.getAmount())
                .status(PaymentStatus.READY)
                .method(command.getPaymentMethod())
                .userId(command.getUserId())
                .orderId(command.getOrderId())
                .build();

        paymentRepository.save(payment);

        return PaymentResult.Pay.from(payment);
    }

    @Transactional
    public PaymentResult.Pay pay(PaymentCommand.Pay command) {
        Payment payment = Payment.builder()
                .amount(command.getAmount())
                .status(PaymentStatus.PAID)
                .method(command.getPaymentMethod())
                .userId(command.getUserId())
                .orderId(command.getOrderId())
                .build();

        paymentRepository.save(payment);

        return PaymentResult.Pay.from(payment);
    }

}
