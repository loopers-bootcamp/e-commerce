package com.loopers.domain.metric.attribute;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum MetricWeight {

    VIEW(0.0001),
    LIKE(0.001),
    SALE(0.01);

    private final double value;

    public static double[] toArray() {
        return Arrays.stream(values()).mapToDouble(MetricWeight::getValue).toArray();
    }

}
