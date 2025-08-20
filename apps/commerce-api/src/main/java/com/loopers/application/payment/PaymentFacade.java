package com.loopers.application.payment;

import com.loopers.application.payment.processor.PaymentProcessContext;
import com.loopers.application.payment.processor.PaymentProcessor;
import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.order.OrderResult;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.error.PaymentErrorType;
import com.loopers.domain.user.UserResult;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentFacade {

    private final UserService userService;
    private final OrderService orderService;
    private final List<PaymentProcessor> processors;

    public PaymentOutput.Pay pay(PaymentInput.Pay input) {
        UserResult.GetUser user = userService.getUser(input.getUserName())
                .orElseThrow(() -> new BusinessException(CommonErrorType.UNAUTHENTICATED));

        UUID orderId = input.getOrderId();

        OrderCommand.GetOrderDetail orderCommand = OrderCommand.GetOrderDetail.builder()
                .orderId(orderId)
                .userId(user.getUserId())
                .build();
        OrderResult.GetOrderDetail order = orderService.getOrderDetail(orderCommand)
                .orElseThrow(() -> new BusinessException(CommonErrorType.NOT_FOUND));

        if (!order.getStatus().isPayable()) {
            throw new BusinessException(PaymentErrorType.UNPROCESSABLE);
        }

        PaymentProcessor paymentProcessor = processors.stream()
                .filter(processor -> processor.supports(input.getPaymentMethod()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(CommonErrorType.INTERNAL_ERROR, "결제를 처리할 수 없습니다."));

        PaymentProcessContext context = PaymentProcessContext.of(user.getUserId(), order);
        return paymentProcessor.process(context);
    }

}
