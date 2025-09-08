package com.loopers.interfaces.consumer.audit;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.application.audit.AuditFacade;
import com.loopers.application.audit.AuditInput;
import com.loopers.config.kafka.KafkaConfig;
import com.loopers.domain.KafkaMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditKafkaConsumer {

    private final ObjectMapper objectMapper;
    private final AuditFacade auditFacade;

    @KafkaListener(
            topics = "${loopers.kafka.topics.DomainEvent.Audit}",
            containerFactory = KafkaConfig.BATCH_LISTENER
    )
    public void onDomainAudited(
            List<ConsumerRecord<String, byte[]>> messages,
            Acknowledgment acknowledgment
    ) throws IOException {
        String topicName = messages.getFirst().topic();
        log.info("Received {} messages on '{}'", messages.size(), topicName);

        List<AuditInput.Audit.Item> items = new ArrayList<>();
        for (ConsumerRecord<String, byte[]> message : messages) {
            KafkaMessage<DomainEvent.Audit> kafkaMessage = objectMapper.readValue(message.value(), new TypeReference<>() {
            });

            AuditInput.Audit.Item item = new AuditInput.Audit.Item(
                    kafkaMessage.eventId(),
                    kafkaMessage.payload().eventKey(),
                    kafkaMessage.payload().eventName(),
                    kafkaMessage.payload().userId()
            );
            items.add(item);
        }

        AuditInput.Audit input = new AuditInput.Audit(topicName, List.copyOf(items));
        auditFacade.audit(input);

        // Manual ack
        acknowledgment.acknowledge();
    }

}
