package com.loopers.interfaces.consumer.audit;

import com.loopers.application.audit.AuditFacade;
import com.loopers.application.audit.AuditInput;
import com.loopers.config.kafka.KafkaConfig;
import com.loopers.domain.KafkaMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditKafkaConsumer {

    private final AuditFacade auditFacade;

    @KafkaListener(
            topics = "${loopers.kafka.topics.DomainEvent.Audit}",
            containerFactory = KafkaConfig.BATCH_LISTENER
    )
    public void onDomainAudited(
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_KEY) List<String> keys,
            @Payload List<KafkaMessage<DomainEvent.Audit>> messages,
            Acknowledgment acknowledgment
    ) {
        log.info("Received {} messages on '{}'", messages.size(), topic);

        List<AuditInput.Audit.Item> items = messages.stream()
                .map(message -> new AuditInput.Audit.Item(
                        message.eventId(),
                        message.payload().eventKey(),
                        message.payload().eventName(),
                        message.payload().userId()
                ))
                .toList();
        AuditInput.Audit input = new AuditInput.Audit(topic, items);
        auditFacade.audit(input);

        // Manual ack
        acknowledgment.acknowledge();
    }

}
