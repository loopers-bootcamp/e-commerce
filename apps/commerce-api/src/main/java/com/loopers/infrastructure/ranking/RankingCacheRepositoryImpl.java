package com.loopers.infrastructure.ranking;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.ranking.RankingCacheRepository;
import com.loopers.domain.ranking.RankingQueryCommand;
import com.loopers.domain.ranking.RankingQueryResult;
import com.loopers.support.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.connection.zset.Aggregate;
import org.springframework.data.redis.connection.zset.Weights;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class RankingCacheRepositoryImpl implements RankingCacheRepository {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

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
                .sorted(Comparator.<ZSetOperations.TypedTuple<String>>
                                comparingDouble(ZSetOperations.TypedTuple::getScore)
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
    public Page<RankingQueryResult.SearchRankings> searchRankings(LocalDate date, Pageable pageable) {
        // page 1번을 offset 0으로 변환한다.
        Pageable pageRequest = pageable.withPage(pageable.getPageNumber() - 1);

        String day = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String key = "product.ranking:" + day;

        long start = pageRequest.getOffset();
        long end = pageRequest.getOffset() + pageRequest.getPageSize();
        List<String> jsonList = stringRedisTemplate.opsForList().range(key, start, end);

        if (CollectionUtils.isEmpty(jsonList)) {
            return Page.empty(pageRequest);
        }

        List<RankingQueryResult.SearchRankings> rankings = jsonList.stream()
                .map(json -> parseJson(json, RankingQueryResult.SearchRankings.class))
                .filter(Objects::nonNull)
                .toList();

        return PageableExecutionUtils.getPage(rankings, pageRequest, () -> stringRedisTemplate.opsForList().size(key));
    }

    @Override
    public void saveRankings(LocalDate date, List<RankingQueryCommand.SaveRankings> rankings) {
        if (CollectionUtils.isEmpty(rankings)) {
            return;
        }

        String day = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String key = "product.ranking:" + day;

        List<String> jsonList = rankings.stream()
                .map(this::toJson)
                .filter(Objects::nonNull)
                .toList();

        // Idempotent
        stringRedisTemplate.delete(key);
        stringRedisTemplate.opsForList().rightPushAll(key, jsonList);
    }

    // -------------------------------------------------------------------------------------------------

    private <T> T parseJson(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private String toJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

}
