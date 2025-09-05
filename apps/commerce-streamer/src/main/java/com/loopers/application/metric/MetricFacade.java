package com.loopers.application.metric;

import com.loopers.domain.audit.AuditCommand;
import com.loopers.domain.audit.AuditService;
import com.loopers.domain.metric.MetricCommand;
import com.loopers.domain.metric.MetricService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MetricFacade {

    private final MetricService metricService;
    private final AuditService auditService;

    @Transactional
    public void aggregateProduct(MetricInput.AggregateProduct input) {
        input.items()
                .stream()
                // Idempotent
                .filter(item -> auditService.handle(new AuditCommand.Handle(item.eventId(), input.topicName())))
                .map(item -> new MetricCommand.AggregateProduct(
                        item.date(),
                        item.productId(),
                        item.likeCount(),
                        item.saleQuantity(),
                        item.viewCount()
                ))
                .forEach(metricService::aggregateProduct);
    }

}
