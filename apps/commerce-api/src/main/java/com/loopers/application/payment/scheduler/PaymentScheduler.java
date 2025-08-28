package com.loopers.application.payment.scheduler;

import com.loopers.application.payment.PaymentFacade;
import com.loopers.application.payment.PaymentInput;
import com.loopers.domain.payment.PaymentResult;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.payment.attribute.PaymentMethod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.stream.Collectors.groupingBy;

@Slf4j
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
    public void reconcilePaymentsByCard() {
        PaymentResult.GetReadyPayments payments = paymentService.getReadyPayments(PaymentMethod.CARD);

        for (PaymentResult.GetReadyPayments.Item paymentItem : payments.getItems()) {
            UUID orderId = paymentItem.getOrderId();
            List<PaymentResult.GetTransactions.Item> transactions = paymentService.getTransactions(orderId)
                    .map(PaymentResult.GetTransactions::getItems)
                    .orElseGet(List::of);

            if (CollectionUtils.isEmpty(transactions)) {
                continue;
            }

            Map<String, List<PaymentInput.Conclude>> statusMap = transactions
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

            try {
                if (statusMap.containsKey("SUCCESS")) {
                    paymentFacade.conclude(statusMap.get("SUCCESS").getFirst());
                } else if (statusMap.containsKey("FAILED") && !statusMap.containsKey("PENDING")) {
                    // 진행중인 결제 건이 하나라도 있으면, 실패한 결제라고 단언할 수 없다.
                    paymentFacade.conclude(statusMap.get("FAILED").getFirst());
                }
            } catch (Exception e) {
                // 보정에 실패해도, 다른 결제 건을 속행한다.
                log.error("Failed to reconcile payment: (orderId={}, message={})", orderId, e.getMessage());
            }
        }
    }

}
