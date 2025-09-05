package com.loopers.infrastructure.audit;

import com.loopers.domain.audit.AuditRepository;
import com.loopers.domain.audit.EventHandled;
import com.loopers.domain.audit.EventLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AuditRepositoryImpl implements AuditRepository {

    private final EventLogJpaRepository eventLogJpaRepository;
    private final EventHandledJpaRepository eventHandledJpaRepository;

    @Override
    public boolean saveIfAbsent(EventLog eventLog) {
        eventLog.prePersist();
        return eventLogJpaRepository.insertIfNotExists(
                eventLog.getId(),
                eventLog.getEventKey(),
                eventLog.getEventName(),
                eventLog.getUserId(),
                eventLog.getCreatedAt(),
                eventLog.getUpdatedAt()
        ) == 1;
    }

    @Override
    public boolean saveIfAbsent(EventHandled eventHandled) {
        eventHandled.prePersist();
        return eventHandledJpaRepository.insertIfNotExists(
                eventHandled.getId(),
                eventHandled.getTopicName(),
                eventHandled.getCreatedAt(),
                eventHandled.getUpdatedAt()
        ) == 1;
    }

}
