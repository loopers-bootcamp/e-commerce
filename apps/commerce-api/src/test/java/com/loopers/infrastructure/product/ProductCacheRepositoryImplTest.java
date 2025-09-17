package com.loopers.infrastructure.product;

import com.fasterxml.jackson.core.type.TypeReference;
import com.loopers.config.RedisCacheConfig;
import com.loopers.config.jackson.WrappedJsonMapper;
import com.loopers.domain.product.ProductCacheRepository;
import com.loopers.domain.product.ProductQueryCommand;
import com.loopers.domain.product.ProductQueryResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ProductCacheRepositoryImplTest {

    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOps;
    private HashOperations<String, String, String> hashOps;
    private WrappedJsonMapper jsonMapper;

    private ProductCacheRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        valueOps = mock(ValueOperations.class);
        hashOps = mock(HashOperations.class);
        jsonMapper = mock(WrappedJsonMapper.class);

        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(redisTemplate.<String, String>opsForHash()).thenReturn(hashOps);

        repository = new ProductCacheRepositoryImpl(redisTemplate, jsonMapper);
    }

    private ProductQueryCommand.SearchProducts mkSearchCmd(String keyword, Long brandId, String sort, int page, int size) {
        // If the real class has a builder/static factory, replace with that.
        return new ProductQueryCommand.SearchProducts(keyword, brandId, sort, page, size);
    }

    private ProductQueryResult.Products mkProduct(String id, String name) {
        // Adapt to the real constructor/factory if needed
        return new ProductQueryResult.Products(id, name, null, null, null, null);
    }

    private ProductQueryResult.ProductDetail mkDetail(String id) {
        // Adapt to the real constructor/factory if needed
        return new ProductQueryResult.ProductDetail(id, "name", "brand", "desc", List.of(), List.of());
    }

    @Nested
    @DisplayName("searchProducts")
    class SearchProductsTests {

        @Test
        @DisplayName("returns empty when pageNumber > 2 (no Redis calls)")
        void returnsEmptyWhenPageGreaterThanTwo() {
            var cmd = mkSearchCmd("k", 1L, "POPULAR", 3, 20);

            Page<ProductQueryResult.Products> page = repository.searchProducts(cmd);

            assertTrue(page.isEmpty());
            verify(redisTemplate, never()).opsForValue();
            verifyNoInteractions(valueOps, jsonMapper);
        }

        @Test
        @DisplayName("returns empty when Redis value missing or blank")
        void returnsEmptyWhenRedisMissing() {
            var cmd = mkSearchCmd("k", 1L, "POPULAR", 1, 10);
            String key = "product.page:" + Objects.hash(cmd.getKeyword(), cmd.getBrandId(), cmd.getSort(), cmd.getPage(), cmd.getSize());

            when(valueOps.getAndExpire(eq(key), eq(Duration.ofSeconds(1)))).thenReturn(null);

            Page<ProductQueryResult.Products> page = repository.searchProducts(cmd);

            assertTrue(page.isEmpty());
            verify(redisTemplate).opsForValue();
            verify(valueOps).getAndExpire(eq(key), eq(Duration.ofSeconds(1)));
            verifyNoInteractions(jsonMapper);
        }

        @Test
        @DisplayName("returns empty when JSON mapper returns null Page")
        void returnsEmptyWhenMapperReturnsNull() {
            var cmd = mkSearchCmd("k", 1L, "POPULAR", 2, 5);
            String key = "product.page:" + Objects.hash(cmd.getKeyword(), cmd.getBrandId(), cmd.getSort(), cmd.getPage(), cmd.getSize());

            when(valueOps.getAndExpire(eq(key), eq(Duration.ofSeconds(1)))).thenReturn("{json}");
            when(jsonMapper.readValue(eq("{json}"), any(TypeReference.class))).thenReturn(null);

            Page<ProductQueryResult.Products> page = repository.searchProducts(cmd);

            assertTrue(page.isEmpty());
            verify(jsonMapper).readValue(eq("{json}"), any(TypeReference.class));
        }

        @Test
        @DisplayName("returns cached Page when present")
        void returnsCachedPage() {
            var cmd = mkSearchCmd("shoe", 2L, "RECENT", 0, 2);
            String key = "product.page:" + Objects.hash(cmd.getKeyword(), cmd.getBrandId(), cmd.getSort(), cmd.getPage(), cmd.getSize());

            var products = List.of(mkProduct("p1", "A"), mkProduct("p2", "B"));
            PageImpl<ProductQueryResult.Products> cached = new PageImpl<>(products, PageRequest.of(0, 2), 5);

            when(valueOps.getAndExpire(eq(key), eq(Duration.ofSeconds(1)))).thenReturn("{json}");
            when(jsonMapper.readValue(eq("{json}"), any(TypeReference.class))).thenReturn(cached);

            Page<ProductQueryResult.Products> page = repository.searchProducts(cmd);

            assertFalse(page.isEmpty());
            assertEquals(2, page.getContent().size());
            assertEquals(5, page.getTotalElements());
        }

        @Test
        @DisplayName("handles Redis get exception gracefully and returns empty")
        void handlesRedisException() {
            var cmd = mkSearchCmd("hat", 3L, "PRICE_ASC", 1, 10);
            String key = "product.page:" + Objects.hash(cmd.getKeyword(), cmd.getBrandId(), cmd.getSort(), cmd.getPage(), cmd.getSize());

            when(valueOps.getAndExpire(eq(key), eq(Duration.ofSeconds(1)))).thenThrow(new RuntimeException("redis down"));
            when(redisTemplate.opsForValue()).thenReturn(valueOps);

            Page<ProductQueryResult.Products> page = repository.searchProducts(cmd);

            assertTrue(page.isEmpty());
            verify(valueOps).getAndExpire(eq(key), eq(Duration.ofSeconds(1)));
            verifyNoInteractions(jsonMapper);
        }
    }

    @Nested
    @DisplayName("saveProducts")
    class SaveProductsTests {

        @Test
        @DisplayName("does nothing when pageNumber > 2")
        void doesNothingWhenPageGreaterThanTwo() {
            var cmd = mkSearchCmd("k", 1L, "POPULAR", 5, 20);
            Page<ProductQueryResult.Products> page = Page.empty();

            repository.saveProducts(cmd, page);

            verifyNoInteractions(jsonMapper);
            verify(redisTemplate, never()).opsForValue();
            verifyNoInteractions(valueOps);
        }

        @Test
        @DisplayName("stores JSON in Redis with TTL=5s and correct key")
        void storesWithTtl5s() {
            var cmd = mkSearchCmd("shoe", 9L, "RECENT", 1, 3);
            var products = List.of(mkProduct("p1", "A"));
            Page<ProductQueryResult.Products> page = new PageImpl<>(products, PageRequest.of(1,3), 4);

            String expectedJson = "{\"ok\":true}";
            when(jsonMapper.writeValueAsString(page)).thenReturn(expectedJson);

            String expectedKey = "product.page:" + Objects.hash(cmd.getKeyword(), cmd.getBrandId(), cmd.getSort(), cmd.getPage(), cmd.getSize());

            repository.saveProducts(cmd, page);

            verify(jsonMapper).writeValueAsString(page);
            verify(redisTemplate).opsForValue();
            verify(valueOps).set(eq(expectedKey), eq(expectedJson), eq(Duration.ofSeconds(5)));
        }

        @Test
        @DisplayName("handles Redis set exception gracefully")
        void handlesRedisSetException() {
            var cmd = mkSearchCmd("hat", 2L, "POPULAR", 0, 10);
            Page<ProductQueryResult.Products> page = Page.empty();
            when(jsonMapper.writeValueAsString(page)).thenReturn("{}");

            String key = "product.page:" + Objects.hash(cmd.getKeyword(), cmd.getBrandId(), cmd.getSort(), cmd.getPage(), cmd.getSize());
            doThrow(new RuntimeException("boom")).when(valueOps).set(eq(key), eq("{}"), eq(Duration.ofSeconds(5)));

            repository.saveProducts(cmd, page);

            verify(valueOps).set(eq(key), eq("{}"), eq(Duration.ofSeconds(5)));
        }
    }

    @Nested
    @DisplayName("findDetail")
    class FindDetailTests {

        @Test
        @DisplayName("returns empty when hash is empty")
        void returnsEmptyWhenNoHash() {
            Long id = 101L;
            when(hashOps.entries("product.detail:" + id)).thenReturn(Map.of());

            Optional<ProductQueryResult.ProductDetail> result = repository.findDetail(id);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("returns ProductDetail.EMPTY when __null__ sentinel present")
        void returnsEmptySentinel() {
            Long id = 202L;
            when(hashOps.entries("product.detail:" + id)).thenReturn(Map.of("__null__", "null"));

            Optional<ProductQueryResult.ProductDetail> result = repository.findDetail(id);

            assertTrue(result.isPresent());
            assertSame(ProductQueryResult.ProductDetail.EMPTY, result.get());
        }

        @Test
        @DisplayName("returns empty when mapper returns null")
        void returnsEmptyWhenMapperNull() {
            Long id = 303L;
            when(hashOps.entries("product.detail:" + id)).thenReturn(Map.of("a","b"));
            when(jsonMapper.readMap(anyMap(), any(TypeReference.class))).thenReturn(null);

            Optional<ProductQueryResult.ProductDetail> result = repository.findDetail(id);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("returns mapped detail when present")
        void returnsMappedDetail() {
            Long id = 404L;
            var cache = Map.of("id", "404");
            var detail = mkDetail("404");

            when(hashOps.entries("product.detail:" + id)).thenReturn(cache);
            when(jsonMapper.readMap(eq(cache), any(TypeReference.class))).thenReturn(detail);

            Optional<ProductQueryResult.ProductDetail> result = repository.findDetail(id);

            assertTrue(result.isPresent());
            assertEquals("404", result.get().id());
        }
    }

    @Nested
    @DisplayName("saveDetail")
    class SaveDetailTests {

        @Test
        @DisplayName("stores __null__ sentinel with jittered TTL when detail is null")
        void storesNullSentinel() {
            Long id = 505L;

            try (MockedStatic<RedisCacheConfig> mocked = Mockito.mockStatic(RedisCacheConfig.class)) {
                mocked.when(() -> RedisCacheConfig.jitter(any())).thenReturn(Duration.ofMinutes(30));

                repository.saveDetail(id, null);

                InOrder inOrder = inOrder(hashOps, redisTemplate);
                inOrder.verify(hashOps).putAll("product.detail:" + id, Map.of("__null__", "null"));
                verify(redisTemplate).expire(eq("product.detail:" + id), eq(Duration.ofMinutes(30)));
            }
        }

        @Test
        @DisplayName("stores mapped hash with jittered TTL when detail provided")
        void storesMappedHash() {
            Long id = 606L;
            var detail = mkDetail("606");
            var map = Map.of("id","606","name","name");

            when(jsonMapper.writeValueAsMap(detail)).thenReturn(map);

            try (MockedStatic<RedisCacheConfig> mocked = Mockito.mockStatic(RedisCacheConfig.class)) {
                mocked.when(() -> RedisCacheConfig.jitter(any())).thenReturn(Duration.ofMinutes(28));

                repository.saveDetail(id, detail);

                verify(hashOps).putAll("product.detail:" + id, map);
                verify(redisTemplate).expire(eq("product.detail:" + id), eq(Duration.ofMinutes(28)));
            }
        }
    }
}

// --- Appended tests below ---
// Note: Using JUnit 5 and Mockito based on repository conventions.
import com.fasterxml.jackson.core.type.TypeReference;
import com.loopers.config.RedisCacheConfig;
import com.loopers.config.jackson.WrappedJsonMapper;
import com.loopers.domain.product.ProductQueryCommand;
import com.loopers.domain.product.ProductQueryResult;
import org.junit.jupiter.api.*;
import org.mockito.InOrder;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ProductCacheRepositoryImpl_AdditionalTests {

    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOps;
    private HashOperations<String, String, String> hashOps;
    private WrappedJsonMapper jsonMapper;
    private ProductCacheRepositoryImpl repository;

    @BeforeEach
    void init() {
        redisTemplate = mock(StringRedisTemplate.class);
        valueOps = mock(ValueOperations.class);
        hashOps = mock(HashOperations.class);
        jsonMapper = mock(WrappedJsonMapper.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(redisTemplate.<String, String>opsForHash()).thenReturn(hashOps);
        repository = new ProductCacheRepositoryImpl(redisTemplate, jsonMapper);
    }

    private ProductQueryCommand.SearchProducts mkCmd(int page, int size) {
        return new ProductQueryCommand.SearchProducts("kw", 7L, "POPULAR", page, size);
    }

    @Test
    void searchProducts_blankStringFromRedis_treatedAsEmpty() {
        var cmd = mkCmd(1, 10);
        String key = "product.page:" + Objects.hash(cmd.getKeyword(), cmd.getBrandId(), cmd.getSort(), cmd.getPage(), cmd.getSize());
        when(valueOps.getAndExpire(eq(key), eq(Duration.ofSeconds(1)))).thenReturn("   ");

        Page<ProductQueryResult.Products> res = repository.searchProducts(cmd);
        assertTrue(res.isEmpty());
        verifyNoInteractions(jsonMapper);
    }

    @Test
    void saveProducts_serializesAndStores() {
        var cmd = mkCmd(0, 2);
        var page = new PageImpl<ProductQueryResult.Products>(List.of(), PageRequest.of(0,2), 0);
        when(jsonMapper.writeValueAsString(page)).thenReturn("{\"p\":1}");
        String key = "product.page:" + Objects.hash(cmd.getKeyword(), cmd.getBrandId(), cmd.getSort(), cmd.getPage(), cmd.getSize());

        repository.saveProducts(cmd, page);

        verify(valueOps).set(eq(key), eq("{\"p\":1}"), eq(Duration.ofSeconds(5)));
    }

    @Test
    void findDetail_readsAndMaps() {
        long id = 999L;
        var cache = Map.of("id","999");
        when(hashOps.entries("product.detail:" + id)).thenReturn(cache);
        var detail = new ProductQueryResult.ProductDetail("999","n","b","d", List.of(), List.of());
        when(jsonMapper.readMap(eq(cache), any(TypeReference.class))).thenReturn(detail);

        Optional<ProductQueryResult.ProductDetail> res = repository.findDetail(id);
        assertTrue(res.isPresent());
        assertEquals("999", res.get().id());
    }
}
