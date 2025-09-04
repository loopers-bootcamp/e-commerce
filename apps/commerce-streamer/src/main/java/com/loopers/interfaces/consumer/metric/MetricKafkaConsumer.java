package com.loopers.interfaces.consumer.metric;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.config.kafka.KafkaConfig;
import com.loopers.domain.KafkaMessage;
import com.loopers.domain.metric.MetricCommand;
import com.loopers.domain.metric.MetricService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MetricKafkaConsumer {

    private final MetricService metricService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${loopers.kafka.topics.ActivityEvent.Like}",
            containerFactory = KafkaConfig.BATCH_LISTENER
    )
    public void onUserLiked(
            List<ConsumerRecord<String, byte[]>> messages,
            Acknowledgment acknowledgment
    ) throws IOException {
        log.info("Received {} messages on '{}'", messages.size(), messages.getFirst().topic());

        for (ConsumerRecord<String, byte[]> message : messages) {
            KafkaMessage<ActivityEvent.Like> kafkaMessage = objectMapper.readValue(message.value(), new TypeReference<>() {
            });

            LocalDate date = kafkaMessage.publishedAt().toLocalDate();
            Long productId = kafkaMessage.payload().productId();

            MetricCommand.AggregateProduct command = MetricCommand.AggregateProduct.ofLikeCount(date, productId, 1L);
            metricService.aggregateProduct(command);
        }

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
        log.info("Received {} messages on '{}'", messages.size(), messages.getFirst().topic());

        for (ConsumerRecord<String, byte[]> message : messages) {
            KafkaMessage<ActivityEvent.Dislike> kafkaMessage = objectMapper.readValue(message.value(), new TypeReference<>() {
            });

            LocalDate date = kafkaMessage.publishedAt().toLocalDate();
            Long productId = kafkaMessage.payload().productId();

            MetricCommand.AggregateProduct command = MetricCommand.AggregateProduct.ofLikeCount(date, productId, -1L);
            metricService.aggregateProduct(command);
        }

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
        log.info("Received {} messages on '{}'", messages.size(), messages.getFirst().topic());

        for (ConsumerRecord<String, byte[]> message : messages) {
            KafkaMessage<ActivityEvent.View> kafkaMessage = objectMapper.readValue(message.value(), new TypeReference<>() {
            });

            LocalDate date = kafkaMessage.publishedAt().toLocalDate();
            Long productId = kafkaMessage.payload().productId();

            MetricCommand.AggregateProduct command = MetricCommand.AggregateProduct.ofViewCount(date, productId, 1L);
            metricService.aggregateProduct(command);
        }

        acknowledgment.acknowledge();
    }

}
