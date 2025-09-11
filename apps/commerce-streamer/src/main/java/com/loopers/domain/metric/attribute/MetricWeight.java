package com.loopers.domain.metric.attribute;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.zset.Weights;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum MetricWeight {

    VIEW(0.0001),
    LIKE(0.001),
    SALE(0.01);

    private final double value;

    public static Weights toWeights() {
        return Weights.of(Arrays.stream(values()).mapToDouble(MetricWeight::getValue).toArray());
    }

}
