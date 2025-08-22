package com.loopers.application.payment.scheduler;

import com.loopers.application.payment.PaymentFacade;
import com.loopers.application.payment.PaymentInput;
import com.loopers.domain.payment.PaymentResult;
import com.loopers.domain.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.stream.Collectors.groupingBy;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class PaymentScheduler {

    private final PaymentFacade paymentFacade;
    private final PaymentService paymentService;

    /**
     * 애플리케이션 시작하고 10분 후부터, 30분 간격으로 실행한다.
     */
    @Scheduled(fixedDelayString = "PT30M", initialDelayString = "PT10M")
    public void reconcilePayments() {
        PaymentResult.GetReadyPayments payments = paymentService.getReadyPayments();

        for (PaymentResult.GetReadyPayments.Item paymentItem : payments.getItems()) {
            UUID orderId = paymentItem.getOrderId();
            PaymentResult.GetTransactions transactions = paymentService.getTransactions(orderId);

            if (CollectionUtils.isEmpty(transactions.getItems())) {
                continue;
            }

            Map<String, List<PaymentInput.Conclude>> statusMap = transactions.getItems()
                    .stream()
                    .map(item -> PaymentInput.Conclude.builder()
                            .transactionKey(item.getTransactionKey())
                            .orderId(orderId)
                            .amount(paymentItem.getAmount())
                            .status(item.getStatus())
                            .reason(item.getReason())
                            .build()
                    )
                    .collect(groupingBy(PaymentInput.Conclude::getStatus));

            if (statusMap.containsKey("SUCCESS")) {
                paymentFacade.conclude(statusMap.get("SUCCESS").getFirst());
            } else if (statusMap.containsKey("FAILED") && !statusMap.containsKey("PENDING")) {
                // 진행중인 결제 건이 하나라도 있으면, 실패한 결제라고 단언할 수 없다.
                paymentFacade.conclude(statusMap.get("FAILED").getFirst());
            }
        }
    }

}
