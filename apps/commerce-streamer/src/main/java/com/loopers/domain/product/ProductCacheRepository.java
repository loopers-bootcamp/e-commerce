package com.loopers.domain.product;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ProductCacheRepository {

    void replaceLikeCountsIfAbsent(List<Map.Entry<Long, Long>> entries);

    void evictProductDetails(List<Long> productIds);

    void accumulateProductRanking(
            LocalDate date,
            Long productId,
            Long likeCount,
            Long saleQuantity,
            Long viewCount
    );

}
