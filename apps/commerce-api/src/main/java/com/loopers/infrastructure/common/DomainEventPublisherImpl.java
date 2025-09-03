package com.loopers.infrastructure.common;

import com.loopers.config.kafka.LoopersKafkaProperties;
import com.loopers.domain.KafkaMessage;
import com.loopers.domain.common.DomainEvent;
import com.loopers.domain.common.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DomainEventPublisherImpl implements DomainEventPublisher {

    private final LoopersKafkaProperties properties;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishEvent(DomainEvent.Audit event) {
        KafkaMessage<DomainEvent> message = KafkaMessage.from(event);
        kafkaTemplate.send(properties.getTopic(event), message.eventId(), message);
    }

}
