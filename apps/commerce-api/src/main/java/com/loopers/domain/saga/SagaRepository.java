package com.loopers.domain.saga;

public interface SagaRepository {

    boolean save(Inbox inbox);

    boolean save(Outbox outbox);

}
