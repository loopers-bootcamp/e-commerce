package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.RankingCacheRepository;
import com.loopers.domain.ranking.RankingQueryResult;
import com.loopers.support.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.connection.zset.Aggregate;
import org.springframework.data.redis.connection.zset.Weights;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.util.Comparator.comparing;

@Repository
@RequiredArgsConstructor
public class RankingCacheRepositoryImpl implements RankingCacheRepository {

    private final StringRedisTemplate stringRedisTemplate;

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public List<RankingQueryResult.FindRanks> findRanks(LocalDate date) {
        String day = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String likes = "metric.product.like:" + day;
        String sales = "metric.product.sale:" + day;
        String views = "metric.product.view:" + day;

        Weights weights = Weights.of(0.01, 0.1, 0.001);
        Set<ZSetOperations.TypedTuple<String>> tupleSet = stringRedisTemplate.opsForZSet()
                .unionWithScores(likes, List.of(sales, views), Aggregate.SUM, weights);

        if (CollectionUtils.isEmpty(tupleSet)) {
            return List.of();
        }

        List<ZSetOperations.TypedTuple<String>> tuples = tupleSet.stream()
                .limit(1000)
                .sorted((Comparator<ZSetOperations.TypedTuple>) comparing(ZSetOperations.TypedTuple::getScore)
                        .thenComparing(ZSetOperations.TypedTuple::getValue)
                        .reversed()
                )
                .toList();

        List<RankingQueryResult.FindRanks> results = new ArrayList<>();
        for (int i = 0; i < tuples.size(); i++) {
            long productId = Long.parseLong(StringUtils.invert9sComplement(tuples.get(i).getValue()));
            results.add(new RankingQueryResult.FindRanks(productId, i + 1));
        }

        return List.copyOf(results);
    }

    @Override
    public Page<?> searchRankings(LocalDate date, Integer page, Integer size) {
        PageRequest pageRequest = PageRequest.of(page, size);

        String day = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String likes = "metric.product.like:" + day;
        String sales = "metric.product.sale:" + day;
        String views = "metric.product.view:" + day;

        String destKey = "temp:" + UUID.randomUUID().toString().replace("-", "");

        // Weight: 0.0 ~ 1.0
        Weights weights = Weights.of(0.01, 0.1, 0.001);

        stringRedisTemplate.opsForZSet()
                .unionAndStore(likes, List.of(sales, views), destKey, Aggregate.SUM, weights);
        stringRedisTemplate.expire(destKey, Duration.ofSeconds(10));

        stringRedisTemplate.opsForZSet()
                .reverseRange(destKey, pageRequest.getOffset(), pageRequest.getPageSize())
                .stream()
                .map(StringUtils::invert9sComplement)
                .map(Long::parseLong)
                .toList();

        return null;
    }

}
