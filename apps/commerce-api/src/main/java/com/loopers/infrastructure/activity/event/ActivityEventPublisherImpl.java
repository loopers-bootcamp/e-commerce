package com.loopers.infrastructure.activity.event;

import com.loopers.config.kafka.LoopersKafkaProperties;
import com.loopers.domain.KafkaMessage;
import com.loopers.domain.activity.event.ActivityEvent;
import com.loopers.domain.activity.event.ActivityEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActivityEventPublisherImpl implements ActivityEventPublisher {

    private final LoopersKafkaProperties properties;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishEvent(ActivityEvent.Like event) {
        KafkaMessage<ActivityEvent.Like> message = KafkaMessage.from(event);
        String partitionKey = event.productId().toString();
        kafkaTemplate.send(properties.getTopic(event), partitionKey, message);
    }

    @Override
    public void publishEvent(ActivityEvent.Dislike event) {
        KafkaMessage<ActivityEvent.Dislike> message = KafkaMessage.from(event);
        String partitionKey = event.productId().toString();
        kafkaTemplate.send(properties.getTopic(event), partitionKey, message);
    }

    @Override
    public void publishEvent(ActivityEvent.View event) {
        KafkaMessage<ActivityEvent.View> message = KafkaMessage.from(event);
        String partitionKey = event.productId().toString();
        kafkaTemplate.send(properties.getTopic(event), partitionKey, message);
    }

}
