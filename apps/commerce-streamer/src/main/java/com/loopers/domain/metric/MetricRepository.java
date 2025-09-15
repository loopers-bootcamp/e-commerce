package com.loopers.domain.metric;

import java.util.List;

public interface MetricRepository {

    void increase(List<Metric> metrics);

}
