package com.loopers.infrastructure.product.cache.sort;

import com.loopers.domain.product.Product;
import com.loopers.domain.product.attribute.ProductSearchSortType;
import com.loopers.support.StringUtils;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class CheapProductCacheManager implements SortedProductCacheManager {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public boolean supports(ProductSearchSortType sortType) {
        return sortType == ProductSearchSortType.CHEAP;
    }

    @Override
    public Page<Long> getProductIds(
            @Nullable Long brandId,
            Pageable pageable
    ) {
        ZSetOperations<String, String> zo = redisTemplate.opsForZSet();

        String key = buildKey(brandId, "v1");
        Set<String> members = zo.range(key, pageable.getOffset(), pageable.getPageSize());

        List<Long> productIds = Objects.requireNonNullElseGet(members, Set::<String>of)
                .stream()
                .map(CheapProductCacheManager::unmemberify)
                .toList();

        return PageableExecutionUtils.getPage(productIds, pageable, () -> Objects.requireNonNullElse(zo.size(key), 0L));
    }

    @Override
    public void saveProduct(Product product) {
        ZSetOperations<String, String> zo = redisTemplate.opsForZSet();

        String key = buildKey(product.getBrandId(), "v1");
        String member = memberify(product.getId());

        zo.add(key, member, product.getBasePrice());

        // 멤버의 최대 크기를 제한하여, No TTL 캐시의 문제를 극복한다.
        zo.removeRange(key, 0, -(MAX_MEMBER_SIZE + 1));
    }

    // -------------------------------------------------------------------------------------------------

    private static String buildKey(@Nullable Long brandId, String version) {
        if (brandId == null) {
            return KEY_PREFIX + version + ":sort:cheap";
        } else {
            return KEY_PREFIX + version + ":brand:" + brandId + ":sort:cheap";
        }
    }

    private static String memberify(Long productId) {
        // 낮은가격순은 오름차순으로 정렬하여 조회하는데, tie-break일 때 productId는 내림차순으로 정렬해야 한다.
        // 9의 보수를 활용하여 오름차순 정렬해도 원래의 productId 순서를 유지한다.
        String stringified = Long.toUnsignedString(productId);
        String member = StringUtils.invert9sComplement(stringified);

        // Redis ZSET tie-break: 사전순(Lexicographic) 정렬
        // 따라서 member를 Long.MAX_VALUE 자릿수만큼 Zero Padding한다.
        return "0".repeat(19 - member.length()) + member;
    }

    private static Long unmemberify(String member) {
        // 9의 보수로 변환된 값을 역산한다.
        String inverted = StringUtils.invert9sComplement(member);
        return Long.parseUnsignedLong(inverted);
    }

}
