package com.loopers.application.payment.processor;

import com.loopers.application.payment.PaymentOutput;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentResult;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.payment.attribute.PaymentMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class CardPaymentProcessor implements PaymentProcessor {

    private final PaymentService paymentService;

    @Override
    public boolean supports(PaymentMethod paymentMethod) {
        return paymentMethod == PaymentMethod.CARD;
    }

    @Override
    public PaymentOutput.Ready process(PaymentProcessContext context) {
        PaymentCommand.Ready readyCommand = PaymentCommand.Ready.builder()
                .amount(context.paymentAmount())
                .paymentMethod(PaymentMethod.CARD)
                .cardType(context.cardType())
                .cardNumber(context.cardNumber())
                .userId(context.userId())
                .orderId(context.orderId())
                .build();
        PaymentResult.Ready payment = paymentService.ready(readyCommand);

        return PaymentOutput.Ready.from(payment);
    }

}
