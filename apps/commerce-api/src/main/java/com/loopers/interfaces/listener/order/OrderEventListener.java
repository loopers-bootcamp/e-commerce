package com.loopers.interfaces.listener.order;

import com.loopers.domain.order.ExternalOrderSender;
import com.loopers.domain.order.event.OrderEvent;
import com.loopers.support.annotation.Inboxing;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final ExternalOrderSender externalOrderSender;

    /**
     * {@link Async}: 메인 트랜잭션이 동기적으로 기다려야 할 필요 없음.
     * <p>
     * {@link TransactionPhase#AFTER_COMMIT}: 메인 트랜잭션이 성공해야만 수행하는 로직이다.
     */
    @Async
    @Inboxing(idempotent = true)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendOrder(OrderEvent.Complete event) {
        externalOrderSender.sendOrder(event.orderId());
    }

}
