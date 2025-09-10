package com.loopers.domain.metric;

public interface LegacyMetricRepository {

    boolean merge(ProductMetricDaily metric);

}
