package com.loopers.domain.payment;

import com.loopers.annotation.ReadOnlyTransactional;
import com.loopers.domain.payment.attempt.PaymentAttemptManager;
import com.loopers.domain.payment.attribute.PaymentMethod;
import com.loopers.domain.payment.attribute.PaymentStatus;
import com.loopers.domain.payment.event.PaymentEvent;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    @Delegate
    private final PaymentAttemptManager paymentAttemptManager;
    private final PaymentGateway paymentGateway;

    private final ApplicationEventPublisher eventPublisher;

    @ReadOnlyTransactional
    public Optional<PaymentResult.GetPayment> getPayment(PaymentCommand.GetPayment command) {
        return paymentRepository.findPayment(command.getOrderId())
                .filter(payment -> Objects.equals(payment.getUserId(), command.getUserId()))
                .map(PaymentResult.GetPayment::from);
    }

    @ReadOnlyTransactional
    public PaymentResult.GetReadyPayments getReadyPayments(PaymentMethod method) {
        List<Payment> payments = paymentRepository.findReadyPayments(method);
        return PaymentResult.GetReadyPayments.from(payments);
    }

    @ReadOnlyTransactional
    public Optional<PaymentResult.GetTransactions> getTransactions(UUID orderId) {
        return paymentGateway.findTransactions(orderId)
                .map(response -> PaymentResult.GetTransactions.from(response.transactions()));
    }

    @Transactional
    public PaymentResult.Ready ready(PaymentCommand.Ready command) {
        Payment payment = Payment.builder()
                .amount(command.getAmount())
                .status(PaymentStatus.READY)
                .method(command.getPaymentMethod())
                .cardType(command.getCardType())
                .cardNumber(command.getCardNumber())
                .userId(command.getUserId())
                .orderId(command.getOrderId())
                .build();
        paymentRepository.save(payment);

        eventPublisher.publishEvent(PaymentEvent.Ready.from(payment));

        return PaymentResult.Ready.from(payment);
    }

    @Transactional
    public PaymentResult.Pay pay(PaymentCommand.Pay command) {
        Payment payment = paymentRepository.findPaymentForUpdate(command.getOrderId())
                .orElseGet(() -> Payment.builder()
                        .amount(command.getAmount())
                        .status(PaymentStatus.READY)
                        .method(command.getPaymentMethod())
                        .userId(command.getUserId())
                        .orderId(command.getOrderId())
                        .build());

        payment.pay();
        paymentRepository.save(payment);

        return PaymentResult.Pay.from(payment);
    }

    @Transactional
    public PaymentResult.Conclude conclude(PaymentCommand.Conclude command) {
        PaymentGateway.Response.GetTransactions.Transaction transaction = paymentGateway.findTransactions(command.getOrderId())
                .map(PaymentGateway.Response.GetTransactions::transactions)
                .stream()
                .flatMap(Collection::stream)
                .filter(tx -> tx.transactionKey().equals(command.getTransactionKey()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(CommonErrorType.NOT_FOUND, "거래 건을 찾을 수 없습니다."));

        PaymentGateway.Status status = PaymentGateway.Status.valueOf(command.getStatus());
        if (status != transaction.status()) {
            throw new BusinessException(CommonErrorType.CONFLICT, "거래 상태가 일치하지 않습니다.");
        }

        Payment payment = paymentRepository.findPaymentForUpdate(command.getOrderId())
                .orElseThrow(() -> new BusinessException(CommonErrorType.NOT_FOUND, "결제 건을 찾을 수 없습니다."));

        if (!Objects.equals(payment.getAmount(), command.getAmount())) {
            throw new BusinessException(CommonErrorType.CONFLICT, "결제 금액이 일치하지 않습니다.");
        }

        switch (status) {
            case SUCCESS -> {
                PaymentEvent.Success successEvent = new PaymentEvent.Success(
                        command.getTransactionKey(),
                        command.getOrderId(),
                        payment.getId()
                );
                eventPublisher.publishEvent(successEvent);

                payment.pay();
            }
            case FAILED -> {
                PaymentEvent.Failed failedEvent = new PaymentEvent.Failed(
                        command.getTransactionKey(),
                        command.getReason(),
                        command.getOrderId(),
                        payment.getId()
                );
                eventPublisher.publishEvent(failedEvent);

                payment.fail();
            }
        }

        paymentRepository.save(payment);

        return PaymentResult.Conclude.from(payment);
    }

}
