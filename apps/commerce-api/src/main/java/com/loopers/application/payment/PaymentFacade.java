package com.loopers.application.payment;

import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.order.OrderResult;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentResult;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.payment.error.PaymentErrorType;
import com.loopers.domain.point.PointCommand;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserResult;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentFacade {

    private final PaymentService paymentService;
    private final OrderService orderService;
    private final UserService userService;
    private final ProductService productService;
    private final PointService pointService;

    @Transactional
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

        List<ProductCommand.DeductStocks.Item> items = order.getProducts()
                .stream()
                .map(product -> ProductCommand.DeductStocks.Item.builder()
                        .productOptionId(product.getProductOptionId())
                        .amount(product.getQuantity())
                        .build()
                )
                .toList();

        ProductCommand.DeductStocks productCommand = ProductCommand.DeductStocks.builder().items(items).build();
        productService.deductStocks(productCommand);

        // 결제 금액이 0원이면 포인트를 차감할 필요가 없다.
        Long totalPrice = order.getTotalPrice();
        if (totalPrice > 0) {
            PointCommand.Spend pointCommand = PointCommand.Spend.builder()
                    .amount(totalPrice)
                    .userId(user.getUserId())
                    .build();
            pointService.spend(pointCommand);
        }

        PaymentCommand.Pay paymentCommand = PaymentCommand.Pay.builder()
                .amount(totalPrice)
                .paymentMethod(input.getPaymentMethod())
                .userId(user.getUserId())
                .orderId(orderId)
                .build();
        PaymentResult.Pay paymentResult = paymentService.pay(paymentCommand);

        orderService.complete(orderId);

        return PaymentOutput.Pay.from(paymentResult);
    }

}
