package com.loopers.interfaces.consumer.metric;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.application.metric.MetricFacade;
import com.loopers.application.metric.MetricInput;
import com.loopers.config.kafka.KafkaConfig;
import com.loopers.domain.KafkaMessage;
import com.loopers.interfaces.consumer.product.ProductEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MetricKafkaConsumer {

    private final ObjectMapper objectMapper;
    private final MetricFacade metricFacade;

    @KafkaListener(
            topics = "${loopers.kafka.topics.ActivityEvent.Like}",
            containerFactory = KafkaConfig.BATCH_LISTENER
    )
    public void onUserLiked(
            List<ConsumerRecord<String, byte[]>> messages,
            Acknowledgment acknowledgment
    ) throws IOException {
        String topicName = messages.getFirst().topic();
        log.info("Received {} messages on '{}'", messages.size(), topicName);

        List<MetricInput.AggregateProduct.Item> items = new ArrayList<>();
        for (ConsumerRecord<String, byte[]> message : messages) {
            KafkaMessage<ActivityEvent.Like> kafkaMessage = objectMapper.readValue(message.value(), new TypeReference<>() {
            });

            String eventId = kafkaMessage.eventId();
            LocalDate date = kafkaMessage.publishedAt().toLocalDate();
            Long productId = kafkaMessage.payload().productId();

            items.add(MetricInput.AggregateProduct.Item.ofLikeCount(eventId, date, productId, 1L));
        }

        MetricInput.AggregateProduct input = new MetricInput.AggregateProduct(topicName, List.copyOf(items));
        metricFacade.aggregateProduct(input);

        acknowledgment.acknowledge();
    }

    @KafkaListener(
            topics = "${loopers.kafka.topics.ActivityEvent.Dislike}",
            containerFactory = KafkaConfig.BATCH_LISTENER
    )
    public void onUserDisliked(
            List<ConsumerRecord<String, byte[]>> messages,
            Acknowledgment acknowledgment
    ) throws IOException {
        String topicName = messages.getFirst().topic();
        log.info("Received {} messages on '{}'", messages.size(), topicName);

        List<MetricInput.AggregateProduct.Item> items = new ArrayList<>();
        for (ConsumerRecord<String, byte[]> message : messages) {
            KafkaMessage<ActivityEvent.Like> kafkaMessage = objectMapper.readValue(message.value(), new TypeReference<>() {
            });

            String eventId = kafkaMessage.eventId();
            LocalDate date = kafkaMessage.publishedAt().toLocalDate();
            Long productId = kafkaMessage.payload().productId();

            items.add(MetricInput.AggregateProduct.Item.ofLikeCount(eventId, date, productId, -1L));
        }

        MetricInput.AggregateProduct input = new MetricInput.AggregateProduct(topicName, List.copyOf(items));
        metricFacade.aggregateProduct(input);

        acknowledgment.acknowledge();
    }

    @KafkaListener(
            topics = "${loopers.kafka.topics.ActivityEvent.View}",
            containerFactory = KafkaConfig.BATCH_LISTENER
    )
    public void onUserViewed(
            List<ConsumerRecord<String, byte[]>> messages,
            Acknowledgment acknowledgment
    ) throws IOException {
        String topicName = messages.getFirst().topic();
        log.info("Received {} messages on '{}'", messages.size(), topicName);

        List<MetricInput.AggregateProduct.Item> items = new ArrayList<>();
        for (ConsumerRecord<String, byte[]> message : messages) {
            KafkaMessage<ActivityEvent.View> kafkaMessage = objectMapper.readValue(message.value(), new TypeReference<>() {
            });

            String eventId = kafkaMessage.eventId();
            LocalDate date = kafkaMessage.publishedAt().toLocalDate();
            Long productId = kafkaMessage.payload().productId();

            items.add(MetricInput.AggregateProduct.Item.ofViewCount(eventId, date, productId, 1L));
        }

        MetricInput.AggregateProduct input = new MetricInput.AggregateProduct(topicName, List.copyOf(items));
        metricFacade.aggregateProduct(input);

        acknowledgment.acknowledge();
    }

    @KafkaListener(
            topics = "${loopers.kafka.topics.ProductEvent.Sale}",
            containerFactory = KafkaConfig.BATCH_LISTENER
    )
    public void onProductSale(
            List<ConsumerRecord<String, byte[]>> messages,
            Acknowledgment acknowledgment
    ) throws IOException {
        String topicName = messages.getFirst().topic();
        log.info("Received {} messages on '{}'", messages.size(), topicName);

        List<MetricInput.AggregateProduct.Item> items = new ArrayList<>();
        for (ConsumerRecord<String, byte[]> message : messages) {
            KafkaMessage<ProductEvent.Sale> kafkaMessage = objectMapper.readValue(message.value(), new TypeReference<>() {
            });

            String eventId = kafkaMessage.eventId();
            LocalDate date = kafkaMessage.publishedAt().toLocalDate();
            Long productId = kafkaMessage.payload().productId();
            long saleQuantity = kafkaMessage.payload().quantity().longValue();

            items.add(MetricInput.AggregateProduct.Item.ofSaleQuantity(eventId, date, productId, saleQuantity));
        }

        MetricInput.AggregateProduct input = new MetricInput.AggregateProduct(topicName, List.copyOf(items));
        metricFacade.aggregateProduct(input);

        acknowledgment.acknowledge();
    }

}
