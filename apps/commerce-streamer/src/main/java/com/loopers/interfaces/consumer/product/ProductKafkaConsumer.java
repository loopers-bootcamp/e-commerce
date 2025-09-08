package com.loopers.interfaces.consumer.product;

import com.loopers.config.kafka.KafkaConfig;
import com.loopers.domain.KafkaMessage;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductService;
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
public class ProductKafkaConsumer {

    private final ProductService productService;

    @KafkaListener(
            topics = "${loopers.kafka.topics.ProductEvent.LikeChanged}",
            containerFactory = KafkaConfig.BATCH_LISTENER
    )
    public void onProductLikeCountChanged(
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Payload List<KafkaMessage<ProductEvent.LikeChanged>> messages,
            Acknowledgment acknowledgment
    ) {
        log.info("Received {} messages on '{}'", messages.size(), topic);

        List<ProductCommand.ReplaceLikeCountCaches.Item> items = messages.stream()
                .map(message -> new ProductCommand.ReplaceLikeCountCaches.Item(
                        message.payload().productId(),
                        message.payload().likeCount()
                ))
                .toList();
        ProductCommand.ReplaceLikeCountCaches command = new ProductCommand.ReplaceLikeCountCaches(items);
        productService.replaceLikeCountCaches(command);

        acknowledgment.acknowledge();
    }

    @KafkaListener(
            topics = "${loopers.kafka.topics.ProductEvent.StockChanged}",
            containerFactory = KafkaConfig.BATCH_LISTENER
    )
    public void onProductStockChanged(
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Payload List<KafkaMessage<ProductEvent.StockChanged>> messages,
            Acknowledgment acknowledgment
    ) {
        log.info("Received {} messages on '{}'", messages.size(), topic);

        List<ProductCommand.EvictProductDetailCaches.Item> items = messages.stream()
                .map(message -> new ProductCommand.EvictProductDetailCaches.Item(
                        message.payload().productId(),
                        message.payload().quantity()
                ))
                .toList();
        ProductCommand.EvictProductDetailCaches command = new ProductCommand.EvictProductDetailCaches(items);
        productService.evictProductDetailCaches(command);

        acknowledgment.acknowledge();
    }

}
