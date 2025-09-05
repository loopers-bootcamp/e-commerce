package com.loopers.interfaces.common;

import com.loopers.domain.common.DomainEvent;
import com.loopers.domain.common.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DomainEventListener {

    private final DomainEventPublisher domainEventPublisher;

    @Async
    @EventListener
    public void audit(DomainEvent event) {
        DomainEvent.Audit auditEvent = DomainEvent.Audit.from(event);
        domainEventPublisher.publishEvent(auditEvent);
    }

}
