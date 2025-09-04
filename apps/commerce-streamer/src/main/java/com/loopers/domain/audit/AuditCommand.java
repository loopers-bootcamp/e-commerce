package com.loopers.domain.audit;

public record AuditCommand() {

    public record Audit(
            String eventId,
            String eventKey,
            String eventName,
            Long userId
    ) {
    }

    // -------------------------------------------------------------------------------------------------

    public record Handle(
            String eventId,
            String topicName
    ) {
    }

}
