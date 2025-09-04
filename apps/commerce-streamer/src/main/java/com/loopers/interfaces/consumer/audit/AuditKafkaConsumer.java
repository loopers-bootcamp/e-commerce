package com.loopers.interfaces.consumer.audit;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.config.kafka.KafkaConfig;
import com.loopers.domain.KafkaMessage;
import com.loopers.domain.audit.AuditCommand;
import com.loopers.domain.audit.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditKafkaConsumer {

    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${loopers.kafka.topics.DomainEvent.Audit}",
            containerFactory = KafkaConfig.BATCH_LISTENER
    )
    public void onDomainAudited(
            List<ConsumerRecord<String, byte[]>> messages,
            Acknowledgment acknowledgment
    ) throws IOException {
        log.info("Received {} messages on '{}'", messages.size(), messages.getFirst().topic());

        for (ConsumerRecord<String, byte[]> message : messages) {
            KafkaMessage<DomainEvent.Audit> kafkaMessage = objectMapper.readValue(message.value(), new TypeReference<>() {
            });

            AuditCommand.Audit command = new AuditCommand.Audit(
                    kafkaMessage.eventId(),
                    kafkaMessage.payload().eventKey(),
                    kafkaMessage.payload().eventName(),
                    kafkaMessage.payload().userId()
            );
            auditService.audit(command);
        }

        // Manual ack
        acknowledgment.acknowledge();
    }

}
