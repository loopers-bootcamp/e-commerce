package com.loopers.domain.saga.event;

import java.util.UUID;

public interface SagaEvent {

    UUID eventKey();

    String eventName();

}
