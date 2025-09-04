package com.loopers.interfaces.consumer.product;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.config.kafka.KafkaConfig;
import com.loopers.domain.KafkaMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductKafkaConsumer {

    private final RedisTemplate<String, Object> objectRedisTemplate;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${loopers.kafka.topics.ProductEvent.LikeChanged}",
            containerFactory = KafkaConfig.BATCH_LISTENER
    )
    public void onProductLikeCountChanged(
            List<ConsumerRecord<String, byte[]>> messages,
            Acknowledgment acknowledgment
    ) throws IOException {
        log.info("Received {} messages on '{}'", messages.size(), messages.getFirst().topic());

        // Cache put
        for (ConsumerRecord<String, byte[]> message : messages) {
            String key = "product.detail:" + message.key();
            KafkaMessage<ProductEvent.LikeChanged> kafkaMessage = objectMapper.readValue(message.value(), new TypeReference<>() {
            });

            Long likeCount = kafkaMessage.payload().likeCount();
            objectRedisTemplate.opsForHash().putIfAbsent(key, "likeCount", likeCount);
        }

        // Manual ack
        acknowledgment.acknowledge();
    }

    @KafkaListener(
            topics = "${loopers.kafka.topics.ProductEvent.StockChanged}",
            containerFactory = KafkaConfig.BATCH_LISTENER
    )
    public void onProductStockChanged(
            List<ConsumerRecord<String, byte[]>> messages,
            Acknowledgment acknowledgment
    ) throws IOException {
        log.info("Received {} messages on '{}'", messages.size(), messages.getFirst().topic());

        // Cache evict
        List<String> cacheKeys = new ArrayList<>();
        for (ConsumerRecord<String, byte[]> message : messages) {
            KafkaMessage<ProductEvent.StockChanged> kafkaMessage = objectMapper.readValue(message.value(), new TypeReference<>() {
            });

            // 재고가 소진된 상품만 캐시를 삭제한다.
            if (kafkaMessage.payload().quantity() == 0) {
                Long productId = kafkaMessage.payload().productId();
                cacheKeys.add("product.detail:" + productId);
            }
        }

        objectRedisTemplate.delete(cacheKeys);

        // Manual ack
        acknowledgment.acknowledge();
    }

}
