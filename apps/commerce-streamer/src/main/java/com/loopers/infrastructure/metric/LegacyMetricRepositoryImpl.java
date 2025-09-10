package com.loopers.infrastructure.metric;

import com.loopers.domain.metric.LegacyMetricRepository;
import com.loopers.domain.metric.ProductMetricDaily;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LegacyMetricRepositoryImpl implements LegacyMetricRepository {

    private final ProductMetricDailyJpaRepository productMetricDailyJpaRepository;

    @Override
    public boolean merge(ProductMetricDaily metric) {
        metric.prePersist();
        return productMetricDailyJpaRepository.merge(
                metric.getDate(),
                metric.getLikeCount(),
                metric.getSaleQuantity(),
                metric.getViewCount(),
                metric.getProductId(),
                metric.getCreatedAt(),
                metric.getUpdatedAt()
        ) == 1;
    }

}
