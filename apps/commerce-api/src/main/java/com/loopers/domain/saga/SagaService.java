package com.loopers.domain.saga;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SagaService {

    private final SagaRepository sagaRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void inbound(SagaCommand.Inbound inbound) {
        Inbox inbox = Inbox.builder()
                .eventKey(inbound.eventKey())
                .eventName(inbound.eventName())
                .payload(serialize(inbound.payload()))
                .build();

        sagaRepository.save(inbox);
    }

    @Transactional
    public void outbound(SagaCommand.Outbound outbound) {
        Outbox outbox = Outbox.builder()
                .eventKey(outbound.eventKey())
                .eventName(outbound.eventName())
                .payload(serialize(outbound.payload()))
                .build();

        sagaRepository.save(outbox);
    }

    // -------------------------------------------------------------------------------------------------

    private String serialize(Object payload) {
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
