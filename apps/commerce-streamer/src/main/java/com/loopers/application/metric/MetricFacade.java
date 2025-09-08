package com.loopers.application.metric;

import com.loopers.domain.audit.AuditCommand;
import com.loopers.domain.audit.AuditService;
import com.loopers.domain.metric.MetricCommand;
import com.loopers.domain.metric.MetricService;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MetricFacade {

    private final MetricService metricService;
    private final AuditService auditService;
    private final ProductService productService;

    @Transactional
    public void aggregateProduct(MetricInput.AggregateProduct input) {
        for (MetricInput.AggregateProduct.Item item : input.items()) {
            // Idempotent
            if (!auditService.handle(new AuditCommand.Handle(item.eventId(), input.topicName()))) {
                continue;
            }

            metricService.aggregateProduct(new MetricCommand.AggregateProduct(
                    item.date(),
                    item.productId(),
                    item.likeCount(),
                    item.saleQuantity(),
                    item.viewCount()
            ));

            productService.aggregateRanking(new ProductCommand.AggregateRanking(
                    item.date(),
                    item.productId(),
                    item.likeCount(),
                    item.saleQuantity(),
                    item.viewCount()
            ));
        }
    }

}
