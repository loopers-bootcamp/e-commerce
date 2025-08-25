package com.loopers.domain.saga;

public interface SagaRepository {

    void save(Inbox inbox);

    void save(Outbox outbox);

}
