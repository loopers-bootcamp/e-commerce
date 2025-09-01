package com.loopers.domain.saga.event;

public interface SagaEvent {

    String eventKey();

    String eventName();

}
