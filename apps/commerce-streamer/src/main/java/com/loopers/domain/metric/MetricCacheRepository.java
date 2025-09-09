package com.loopers.domain.metric;

public interface MetricCacheRepository {

    void accumulate(ProductMetricDaily metric);

}
