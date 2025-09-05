package com.loopers.domain.saga.event;

import com.loopers.domain.saga.SagaCommand;
import com.loopers.domain.saga.SagaService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Primary
@Component
@RequiredArgsConstructor
class SagaEventPublisher implements ApplicationEventPublisher {

    private final SagaService sagaService;
    private final ApplicationContext delegate;

    @Override
    public void publishEvent(@NonNull Object event) {
        if (event instanceof SagaEvent sagaEvent) {
            Runnable runnable = () -> {
                SagaCommand.Outbound command = new SagaCommand.Outbound(
                        sagaEvent.eventKey(),
                        sagaEvent.eventName(),
                        sagaEvent
                );
                sagaService.outbound(command);
            };

            if (TransactionSynchronizationManager.isSynchronizationActive()
                    && TransactionSynchronizationManager.isActualTransactionActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void beforeCommit(boolean readOnly) {
                        runnable.run();
                    }
                });
            } else {
                runnable.run();
            }
        }

        delegate.publishEvent(event);
    }

}
