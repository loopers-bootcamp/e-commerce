package com.loopers.infrastructure.product;

import com.loopers.config.ranking.WeightedRankingProperties;
import com.loopers.domain.product.ProductCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ProductCacheRepositoryImpl implements ProductCacheRepository {

    // TODO: https://github.com/spring-projects/spring-data-redis/blob/main/src/main/antora/modules/ROOT/pages/redis/scripting.adoc
    private static final String HSET_IF_KEY_EXISTS = """
            local k = KEYS[1]
            if redis.call('EXISTS', k) == 1 then
              return redis.call('HSET', k, ARGV[1], ARGV[2])
            end
            return -1
            """;

    private final RedisTemplate<String, Object> objectRedisTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final WeightedRankingProperties properties;

    @SuppressWarnings("unchecked")
    @Override
    public void replaceLikeCountsIfAbsent(List<Map.Entry<Long, Long>> entries) {
        objectRedisTemplate.executePipelined((RedisConnection connection) -> {
            RedisSerializer<String> keySerializer = (RedisSerializer<String>) objectRedisTemplate.getKeySerializer();
            RedisSerializer<String> hashKeySerializer = (RedisSerializer<String>) objectRedisTemplate.getHashKeySerializer();
            RedisSerializer<Object> hashValueSerializer = (RedisSerializer<Object>) objectRedisTemplate.getHashValueSerializer();

            // Cache put
            for (Map.Entry<Long, Long> entry : entries) {
                byte[] key = keySerializer.serialize("product.detail:" + entry.getKey());
                byte[] hashKey = hashKeySerializer.serialize("likeCount");
                byte[] hashValue = hashValueSerializer.serialize(entry.getValue());

                // Set value if key exists
                byte[] script = HSET_IF_KEY_EXISTS.getBytes(StandardCharsets.UTF_8);
                connection.scriptingCommands().eval(
                        script,
                        ReturnType.INTEGER,
                        1, // key size
                        key, hashKey, hashValue
                );
            }

            return null;
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public void evictProductDetails(List<Long> productIds) {
        objectRedisTemplate.executePipelined((RedisConnection connection) -> {
            RedisSerializer<String> keySerializer = (RedisSerializer<String>) objectRedisTemplate.getKeySerializer();

            // Cache evict
            byte[][] keys = productIds.stream()
                    .map(productId -> "product.detail:" + productId)
                    .map(keySerializer::serialize)
                    .toArray(byte[][]::new);
            connection.keyCommands().del(keys);

            return null;
        });
    }

    @Override
    public void accumulateProductRanking(
            LocalDate date,
            Long productId,
            Long likeCount,
            Long saleQuantity,
            Long viewCount
    ) {
        String day = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String value = productId.toString();

        Map.of(
                        "product.ranking.top100.likes:" + day, likeCount,
                        "product.ranking.top100.sales:" + day, saleQuantity,
                        "product.ranking.top100.views:" + day, viewCount
                )
                .forEach((k, v) -> {
                    // No TTL, but remove tails
                    stringRedisTemplate.opsForZSet().incrementScore(k, value, v);
                    stringRedisTemplate.opsForZSet().removeRange(k, 0, -101);
                });

//        Weights weights = Weights.of(0.2, 0.7, 0.1);
//        stringRedisTemplate.opsForZSet().unionWithScores("", List.of(), Aggregate.SUM, weights);
    }

}
