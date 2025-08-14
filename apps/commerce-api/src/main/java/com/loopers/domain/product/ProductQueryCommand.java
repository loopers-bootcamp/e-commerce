package com.loopers.domain.product;

import com.loopers.domain.product.attribute.ProductSearchSortType;
import lombok.*;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProductQueryCommand {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class SearchProducts {
        private final List<Long> productIds;

        private final String keyword;
        private final Long brandId;
        private final ProductSearchSortType sort;

        private final Integer page;
        private final Integer size;

        public SearchProducts withProductIds(List<Long> productIds) {
            return builder()
                    .productIds(productIds)
                    .keyword(this.keyword)
                    .brandId(this.brandId)
                    .sort(this.sort)
                    .page(this.page)
                    .size(this.size)
                    .build();
        }
    }

}
