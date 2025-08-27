package com.loopers.infrastructure.saga;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.saga.Inbox;
import com.loopers.domain.saga.Outbox;
import com.loopers.domain.saga.SagaRepository;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
@RequiredArgsConstructor
public class SagaRepositoryImpl implements SagaRepository {

    private final InboxJpaRepository inboxJpaRepository;
    private final OutboxJpaRepository outboxJpaRepository;

    private final ObjectMapper objectMapper;

    @Override
    public boolean save(Inbox inbox) {
        inbox.prePersist();
        return inboxJpaRepository.insertIfNotExists(
                inbox.getEventKey(),
                inbox.getEventName(),
                serialize(inbox.getPayload()),
                inbox.getCreatedAt(),
                inbox.getUpdatedAt()
        ) == 1;
    }

    @Override
    public boolean save(Outbox outbox) {
        outbox.prePersist();
        return outboxJpaRepository.insertIfNotExists(
                outbox.getEventKey(),
                outbox.getEventName(),
                serialize(outbox.getPayload()),
                outbox.getCreatedAt(),
                outbox.getUpdatedAt()
        ) == 1;
    }

    // -------------------------------------------------------------------------------------------------

    private String serialize(Map<String, Object> payload) {
        if (payload == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new BusinessException(CommonErrorType.INTERNAL_ERROR, e.getMessage());
        }
    }

}
