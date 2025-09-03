package com.loopers.infrastructure.audit;

import com.loopers.domain.audit.AuditRepository;
import com.loopers.domain.audit.EventLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AuditRepositoryImpl implements AuditRepository {

    private final EventLogJpaRepository eventLogJpaRepository;

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

}
