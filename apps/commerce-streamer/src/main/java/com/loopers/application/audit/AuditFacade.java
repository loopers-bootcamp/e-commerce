package com.loopers.application.audit;

import com.loopers.domain.audit.AuditCommand;
import com.loopers.domain.audit.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditFacade {

    private final AuditService auditService;

    @Transactional
    public void audit(AuditInput.Audit input) {
        input.items()
                .stream()
                // Idempotent
                .filter(item -> auditService.handle(new AuditCommand.Handle(item.eventId(), input.topicName())))
                .map(item -> new AuditCommand.Audit(
                        item.eventId(),
                        item.eventKey(),
                        item.eventName(),
                        item.userId()
                ))
                .forEach(auditService::audit);
    }

}
