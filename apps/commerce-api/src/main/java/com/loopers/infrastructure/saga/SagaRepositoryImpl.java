package com.loopers.infrastructure.saga;

import com.loopers.domain.saga.Inbox;
import com.loopers.domain.saga.Outbox;
import com.loopers.domain.saga.SagaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SagaRepositoryImpl implements SagaRepository {

    private final InboxJpaRepository inboxJpaRepository;
    private final OutboxJpaRepository outboxJpaRepository;

    @Override
    public void save(Inbox inbox) {
        inbox.prePersist();
        inboxJpaRepository.insertIfNotExists(
                inbox.getEventKey(),
                inbox.getEventName(),
                inbox.getPayload(),
                inbox.getCreatedAt(),
                inbox.getUpdatedAt()
        );
    }

    @Override
    public void save(Outbox outbox) {
        outbox.prePersist();
        outboxJpaRepository.insertIfNotExists(
                outbox.getEventKey(),
                outbox.getEventName(),
                outbox.getPayload(),
                outbox.getCreatedAt(),
                outbox.getUpdatedAt()
        );
    }

}
