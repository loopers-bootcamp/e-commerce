package com.loopers.infrastructure.product.cache;

import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductCacheRepository;
import com.loopers.domain.product.ProductQueryCommand;
import com.loopers.domain.product.ProductQueryResult;
import com.loopers.domain.product.attribute.ProductSearchSortType;
import com.loopers.infrastructure.product.cache.sort.SortedProductCacheManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Repository
@RequiredArgsConstructor
public class ProductCacheRepositoryImpl implements ProductCacheRepository {

    private final List<SortedProductCacheManager> managers;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public Page<Long> searchProductIds(ProductQueryCommand.SearchProducts command) {
        PageRequest pageRequest = PageRequest.of(command.getPage(), command.getSize());

        // 키워드 검색이 있으면 캐시에서 조회할 수 없다.
        if (StringUtils.hasText(command.getKeyword())) {
            return Page.empty(pageRequest);
        }

        Long brandId = command.getBrandId();
        ProductSearchSortType sortType = command.getSort();

        for (SortedProductCacheManager manager : managers) {
            if (manager.supports(sortType)) {
                return manager.getProductIds(brandId, pageRequest);
            }
        }

        return Page.empty(pageRequest);
    }

    @Override
    public List<ProductQueryResult.Products> findProducts(List<Long> productIds) {
        List<Object> fieldNames = Arrays.stream(ProductCache.Simple.V1.Fields.values()).map(Enum::name).collect(toList());
        HashOperations<String, Object, Object> ho = redisTemplate.opsForHash();

        List<ProductQueryResult.Products> results = new ArrayList<>();

        for (Long productId : productIds) {
            String key = "simple:product:" + productId;
            List<Object> row = ho.multiGet(key, fieldNames);
            ProductCache.Simple.V1 cache = ProductCache.Simple.V1.from(row);

            ProductQueryResult.Products result = ProductQueryResult.Products.builder()
                    .productId(cache.productId())
                    .productName(cache.productName())
                    .basePrice(cache.basePrice())
                    .likeCount(cache.likeCount())
                    .brandId(cache.brandId())
                    .brandName(cache.brandName())
                    .build();

            results.add(result);
        }

        return results;
    }

    @Override
    public void saveProduct(Product product) {
        for (SortedProductCacheManager manager : managers) {
            manager.saveProduct(product);
        }
    }

}
