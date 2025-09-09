package com.loopers.domain.product;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductCacheRepository productCacheRepository;

    public void replaceLikeCounts(ProductCommand.ReplaceLikeCounts command) {
        if (CollectionUtils.isEmpty(command.items())) {
            return;
        }

        List<Map.Entry<Long, Long>> entries = command.items()
                .stream()
                .map(item -> Map.entry(item.productId(), item.likeCount()))
                .toList();
        productCacheRepository.replaceLikeCountsIfAbsent(entries);
    }

    public void removeDetails(ProductCommand.RemoveDetails command) {
        if (CollectionUtils.isEmpty(command.items())) {
            return;
        }

        List<Long> productIds = command.items()
                .stream()
                // 재고가 소진된 상품만 캐시를 삭제한다.
                .filter(item -> item.stockQuantity() == 0)
                .map(ProductCommand.RemoveDetails.Item::productId)
                .toList();
        productCacheRepository.evictProductDetails(productIds);
    }

}
