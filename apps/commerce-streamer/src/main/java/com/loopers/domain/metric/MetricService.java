package com.loopers.domain.metric;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MetricService {

    private final MetricRepository metricRepository;

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
    }

}
