package com.loopers.infrastructure.metric;

import com.loopers.domain.metric.MetricCacheRepository;
import com.loopers.domain.metric.ProductMetricDaily;
import com.loopers.support.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.format.DateTimeFormatter;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class MetricCacheRepositoryImpl implements MetricCacheRepository {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void accumulate(ProductMetricDaily metric) {
        String day = metric.getDate().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String member = StringUtils.invert9sComplement("%019d".formatted(metric.getProductId()));

        Map.of(
                        "metric.product.like:" + day, metric.getLikeCount(),
                        "metric.product.sale:" + day, metric.getSaleQuantity(),
                        "metric.product.view:" + day, metric.getViewCount()
                )
                .forEach((key, increment) -> {
                    // No TTL
                    stringRedisTemplate.opsForZSet().incrementScore(key, member, increment);
                });
    }

}
