package com.loopers.infrastructure.product;

import com.fasterxml.jackson.core.type.TypeReference;
import com.loopers.config.RedisCacheConfig;
import com.loopers.config.jackson.WrappedObjectMapper;
import com.loopers.domain.product.ProductCacheRepository;
import com.loopers.domain.product.ProductQueryCommand;
import com.loopers.domain.product.ProductQueryResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ProductCacheRepositoryImpl implements ProductCacheRepository {

    private final StringRedisTemplate stringRedisTemplate;
    private final WrappedObjectMapper wrappedObjectMapper;

    @Override
    public Page<ProductQueryResult.Products> searchProducts(ProductQueryCommand.SearchProducts command) {
        PageRequest pageRequest = PageRequest.of(command.getPage(), command.getSize());

        // 2 페이지까지만 캐싱한다.
        if (pageRequest.getPageNumber() > 2) {
            return Page.empty(pageRequest);
        }

        String key = "product.page:" + Objects.hash(
                command.getKeyword(),
                command.getBrandId(),
                command.getSort(),
                command.getPage(),
                command.getSize()
        );

        String json = null;
        try {
            json = stringRedisTemplate.opsForValue().getAndExpire(key, Duration.ofSeconds(1));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        if (!StringUtils.hasText(json)) {
            return Page.empty(pageRequest);
        }

        PageImpl<ProductQueryResult.Products> page = wrappedObjectMapper.readValue(json, new TypeReference<>() {
        });

        return page == null ? Page.empty(pageRequest) : page;
    }

    @Override
    public void saveProducts(
            ProductQueryCommand.SearchProducts command,
            Page<ProductQueryResult.Products> page
    ) {
        PageRequest pageRequest = PageRequest.of(command.getPage(), command.getSize());

        // 2 페이지까지만 캐싱한다.
        if (pageRequest.getPageNumber() > 2) {
            return;
        }

        String json = wrappedObjectMapper.writeValueAsString(page);
        String key = "product.page:" + Objects.hash(
                command.getKeyword(),
                command.getBrandId(),
                command.getSort(),
                command.getPage(),
                command.getSize()
        );

        try {
            // 모든 검색 조건을 캐싱하는 대신, TTL를 짧게 줘서 과도한 메모리 점유를 방지한다.
            stringRedisTemplate.opsForValue().set(key, json, Duration.ofSeconds(5));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public Optional<ProductQueryResult.ProductDetail> findDetail(Long productId) {
        String key = "product.detail:" + productId;
        Map<String, String> cache = stringRedisTemplate.<String, String>opsForHash().entries(key);

        if (CollectionUtils.isEmpty(cache)) {
            return Optional.empty();
        }

        // 캐시 관통을 방지한다.
        if (cache.containsKey("__null__")) {
            return Optional.of(ProductQueryResult.ProductDetail.EMPTY);
        }

        ProductQueryResult.ProductDetail detail = wrappedObjectMapper.readMap(cache, new TypeReference<>() {
        });
        return Optional.ofNullable(detail);
    }

    @Override
    public void saveDetail(Long productId, ProductQueryResult.ProductDetail detail) {
        String key = "product.detail:" + productId;
        Duration ttl = RedisCacheConfig.jitter(Duration.ofMinutes(30));

        // 캐시 관통을 방지한다.
        if (detail == null) {
            stringRedisTemplate.opsForHash().putAll(key, Map.of("__null__", "null"));
            stringRedisTemplate.expire(key, ttl);
            return;
        }

        Map<String, String> cache = wrappedObjectMapper.writeValueAsMap(detail);
        stringRedisTemplate.opsForHash().putAll(key, cache);
        stringRedisTemplate.expire(key, ttl);
    }

}
