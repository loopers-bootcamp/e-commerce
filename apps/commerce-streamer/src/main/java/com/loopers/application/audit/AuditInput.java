package com.loopers.application.audit;

import java.util.List;

public record AuditInput() {

    public record Audit(
            String topicName,
            List<Item> items
    ) {
        public record Item(
                String eventId,
                String eventKey,
                String eventName,
                Long userId
        ) {
        }
    }

    // -------------------------------------------------------------------------------------------------

    public record Handle(
            String eventId,
            String topicName
    ) {
    }

}
