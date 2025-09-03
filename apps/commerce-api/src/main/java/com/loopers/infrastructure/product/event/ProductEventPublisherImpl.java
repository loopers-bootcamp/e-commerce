package com.loopers.infrastructure.product.event;

import com.loopers.config.kafka.LoopersKafkaProperties;
import com.loopers.domain.KafkaMessage;
import com.loopers.domain.product.event.ProductEvent;
import com.loopers.domain.product.event.ProductEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductEventPublisherImpl implements ProductEventPublisher {

    private final LoopersKafkaProperties properties;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishEvent(ProductEvent.LikeChanged event) {
        KafkaMessage<ProductEvent.LikeChanged> message = KafkaMessage.from(event);
        kafkaTemplate.send(properties.getTopic(event), event.productId().toString(), message);
    }

}
