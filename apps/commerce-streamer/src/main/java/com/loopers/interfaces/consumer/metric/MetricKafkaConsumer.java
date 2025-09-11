package com.loopers.interfaces.consumer.metric;

import com.loopers.application.metric.MetricFacade;
import com.loopers.application.metric.MetricInput;
import com.loopers.config.kafka.KafkaConfig;
import com.loopers.domain.KafkaMessage;
import com.loopers.interfaces.consumer.product.ProductEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MetricKafkaConsumer {

    private final MetricFacade metricFacade;

    @KafkaListener(
            topics = "${loopers.kafka.topics.ActivityEvent.Like}",
            containerFactory = KafkaConfig.BATCH_LISTENER
    )
    public void onUserLiked(
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Payload List<KafkaMessage<ActivityEvent.Like>> messages,
            Acknowledgment acknowledgment
    ) {
        log.info("Received {} messages on '{}'", messages.size(), topic);

        List<MetricInput.AggregateProduct.Item> items = messages.stream()
                .map(message -> MetricInput.AggregateProduct.Item.ofLikeCount(
                        message.eventId(),
                        message.publishedAt().toLocalDate(),
                        message.payload().productId(),
                        1L
                ))
                .toList();
        MetricInput.AggregateProduct input = new MetricInput.AggregateProduct(topic, items);
        metricFacade.aggregateProduct(input);

        acknowledgment.acknowledge();
    }

    @KafkaListener(
            topics = "${loopers.kafka.topics.ActivityEvent.Dislike}",
            containerFactory = KafkaConfig.BATCH_LISTENER
    )
    public void onUserDisliked(
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Payload List<KafkaMessage<ActivityEvent.Dislike>> messages,
            Acknowledgment acknowledgment
    ) {
        log.info("Received {} messages on '{}'", messages.size(), topic);

        List<MetricInput.AggregateProduct.Item> items = messages.stream()
                .map(message -> MetricInput.AggregateProduct.Item.ofLikeCount(
                        message.eventId(),
                        message.publishedAt().toLocalDate(),
                        message.payload().productId(),
                        -1L
                ))
                .toList();
        MetricInput.AggregateProduct input = new MetricInput.AggregateProduct(topic, items);
        metricFacade.aggregateProduct(input);

        acknowledgment.acknowledge();
    }

    @KafkaListener(
            topics = "${loopers.kafka.topics.ActivityEvent.View}",
            containerFactory = KafkaConfig.BATCH_LISTENER
    )
    public void onUserViewed(
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Payload List<KafkaMessage<ActivityEvent.View>> messages,
            Acknowledgment acknowledgment
    ) {
        log.info("Received {} messages on '{}'", messages.size(), topic);

        List<MetricInput.AggregateProduct.Item> items = messages.stream()
                .map(message -> MetricInput.AggregateProduct.Item.ofViewCount(
                        message.eventId(),
                        message.publishedAt().toLocalDate(),
                        message.payload().productId(),
                        1L
                ))
                .toList();
        MetricInput.AggregateProduct input = new MetricInput.AggregateProduct(topic, items);
        metricFacade.aggregateProduct(input);

        acknowledgment.acknowledge();
    }

    @KafkaListener(
            topics = "${loopers.kafka.topics.ProductEvent.Sale}",
            containerFactory = KafkaConfig.BATCH_LISTENER
    )
    public void onProductSale(
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Payload List<KafkaMessage<ProductEvent.Sale>> messages,
            Acknowledgment acknowledgment
    ) {
        log.info("Received {} messages on '{}'", messages.size(), topic);

        List<MetricInput.AggregateProduct.Item> items = messages.stream()
                .map(message -> MetricInput.AggregateProduct.Item.ofSaleQuantity(
                        message.eventId(),
                        message.publishedAt().toLocalDate(),
                        message.payload().productId(),
                        message.payload().quantity().longValue()
                ))
                .toList();
        MetricInput.AggregateProduct input = new MetricInput.AggregateProduct(topic, items);
        metricFacade.aggregateProduct(input);

        acknowledgment.acknowledge();
    }

}
