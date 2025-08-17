package com.loopers.infrastructure.product;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.product.ProductCacheRepository;
import com.loopers.domain.product.ProductQueryCommand;
import com.loopers.domain.product.ProductQueryResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class ProductCacheRepositoryImpl implements ProductCacheRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public Page<ProductQueryResult.Products> searchProducts(ProductQueryCommand.SearchProducts command) {
        PageRequest pageRequest = PageRequest.of(command.getPage(), command.getSize());

        // 2 페이지까지만 캐싱한다.
        if (pageRequest.getPageNumber() > 2) {
            return Page.empty(pageRequest);
        }

        String key = "page:products:" + Objects.hash(command);
        String json = redisTemplate.opsForValue().getAndExpire(key, Duration.ofSeconds(1));

        if (!StringUtils.hasText(json)) {
            return Page.empty(pageRequest);
        }

        try {
            return objectMapper.readValue(json, new TypeReference<PageImpl<ProductQueryResult.Products>>() {
            });
        } catch (JsonProcessingException ignored) {
            return Page.empty(pageRequest);
        }
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

        String json;
        try {
            json = objectMapper.writeValueAsString(page);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        String key = "page:products:" + Objects.hash(
                command.getKeyword(),
                command.getBrandId(),
                command.getSort(),
                command.getPage(),
                command.getSize()
        );

        // 모든 검색 조건을 캐싱하는 대신, TTL를 짧게 줘서 과도한 메모리 점유를 방지한다.
        redisTemplate.opsForValue().set(key, json, Duration.ofSeconds(5));
    }

}
