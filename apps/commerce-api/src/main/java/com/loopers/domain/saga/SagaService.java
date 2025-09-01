package com.loopers.domain.saga;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SagaService {

    private final SagaRepository sagaRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public SagaResult.Inbound inbound(SagaCommand.Inbound inbound) {
        Inbox inbox = Inbox.builder()
                .eventKey(inbound.eventKey())
                .eventName(inbound.eventName())
                .payload(serialize(inbound.payload()))
                .build();

        boolean saved = sagaRepository.save(inbox);

        return SagaResult.Inbound.from(inbox, saved);
    }

    @Transactional
    public SagaResult.Outbound outbound(SagaCommand.Outbound outbound) {
        Outbox outbox = Outbox.builder()
                .eventKey(outbound.eventKey())
                .eventName(outbound.eventName())
                .payload(serialize(outbound.payload()))
                .build();

        boolean saved = sagaRepository.save(outbox);

        return SagaResult.Outbound.from(outbox, saved);
    }

    // -------------------------------------------------------------------------------------------------

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Map<String, Object> serialize(Object payload) {
        if (payload == null) {
            return null;
        }

        if (payload instanceof Map map) {
            return map;
        }

        Class<?> type = payload.getClass();
        if (type.isArray() || type.getPackageName().startsWith("java.")) {
            return Map.of("payload", payload);
        }

        return objectMapper.convertValue(payload, new TypeReference<>() {
        });
    }

}
