package com.loopers.domain.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditRepository auditRepository;

    @Transactional
    public void audit(AuditCommand.Audit command) {
        EventLog eventLog = EventLog.builder()
                .id(command.eventId())
                .eventKey(command.eventKey())
                .eventName(command.eventName())
                .userId(command.userId())
                .build();

        auditRepository.save(eventLog);
    }

}
