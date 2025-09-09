package com.loopers.domain.metric;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MetricService {

    private final MetricRepository metricRepository;
    private final MetricCacheRepository metricCacheRepository;

    @Transactional
    public void aggregateProduct(MetricCommand.AggregateProduct command) {
        ProductMetricDaily metric = ProductMetricDaily.builder()
                .date(command.date())
                .likeCount(command.likeCount())
                .saleQuantity(command.saleQuantity())
                .viewCount(command.viewCount())
                .productId(command.productId())
                .build();

        metricRepository.merge(metric);
        metricCacheRepository.accumulate(metric);
    }

}
