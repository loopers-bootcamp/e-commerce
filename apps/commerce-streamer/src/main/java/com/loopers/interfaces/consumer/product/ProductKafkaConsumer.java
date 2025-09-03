package com.loopers.interfaces.consumer.product;

import com.loopers.config.kafka.KafkaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductKafkaConsumer {

    private final RedisTemplate<String, String> redisTemplate;

    @KafkaListener(
            topics = "${loopers.kafka.topics.ProductEvent.LikeChanged}",
            containerFactory = KafkaConfig.BATCH_LISTENER
    )
    public void onProductLikeCountChanged(
            List<ConsumerRecord<String, Object>> messages,
            Acknowledgment acknowledgment
    ) {
        log.info("Received {} messages on '{}'", messages.size(), messages.getFirst().topic());

        // Cache evict
        List<String> cacheKeys = messages.stream().map(message -> "detail:product::" + message.key()).toList();
        redisTemplate.delete(cacheKeys);

        // Manual ack
        acknowledgment.acknowledge();
    }

}
