package com.loopers.infrastructure.metric;

import com.loopers.domain.metric.Metric;
import com.loopers.domain.metric.MetricRepository;
import com.loopers.domain.metric.attribute.MetricWeight;
import com.loopers.support.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.zset.Aggregate;
import org.springframework.data.redis.connection.zset.Weights;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MetricRepositoryImpl implements MetricRepository {

    private static final long RETENTION_DAYS = 3;

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void increase(List<Metric> metrics) {
        Map<LocalDate, List<Metric>> dateMap = metrics.stream().collect(groupingBy(Metric::getDate));

        stringRedisTemplate.executePipelined((RedisConnection connection) -> {
            dateMap.forEach((date, items) -> {
                String day = date.format(DateTimeFormatter.BASIC_ISO_DATE);
                String views = "metric.product.view:" + day;
                String likes = "metric.product.like:" + day;
                String sales = "metric.product.sale:" + day;

                for (Metric item : items) {
                    String member = StringUtils.invert9sComplement("%019d".formatted(item.getProductId()));
                    ZSetOperations<String, String> zSet = stringRedisTemplate.opsForZSet();

                    // Save with non-weighted score.
                    zSet.incrementScore(views, member, item.getViewCount());
                    zSet.incrementScore(likes, member, item.getLikeCount());
                    zSet.incrementScore(sales, member, item.getSaleQuantity());
                }

                // Save with weighted score: overwrite for each call.
                String all = "metric.product.all:" + day;
                Weights weights = MetricWeight.toWeights();
                stringRedisTemplate.opsForZSet().unionAndStore(views, List.of(likes, sales), all, Aggregate.SUM, weights);

                // Top 1000
                stringRedisTemplate.opsForZSet().removeRange(all, 0, -1001);

                Instant ttl = ZonedDateTime.of(date.plusDays(RETENTION_DAYS), LocalTime.MIN, ZoneId.systemDefault()).toInstant();
                Stream.of(views, likes, sales, all).forEach(key -> stringRedisTemplate.expireAt(key, ttl));

                log.info("Increase {} metrics on '{}', '{}', '{}', '{}'", items.size(), views, likes, sales, all);
            });

            return null;
        });

    }

}
