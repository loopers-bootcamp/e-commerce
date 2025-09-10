package com.loopers.domain.metric;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
public class MetricService {

    private final MetricRepository metricRepository;

    public void aggregate(MetricCommand.Aggregate command) {
        if (CollectionUtils.isEmpty(command.items())) {
            return;
        }

        Collection<Metric> metrics = command.items()
                .stream()
                .map(item -> Metric.builder()
                        .date(item.date())
                        .productId(item.productId())
                        .likeCount(item.likeCount())
                        .saleQuantity(item.saleQuantity())
                        .viewCount(item.viewCount())
                        .build()
                )
                .collect(toMap(
                        Function.identity(),
                        Function.identity(),
                        Metric::plus
                ))
                .values();

        metricRepository.increase(List.copyOf(metrics));
    }

}
