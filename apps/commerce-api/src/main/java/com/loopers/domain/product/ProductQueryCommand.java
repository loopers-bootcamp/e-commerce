package com.loopers.domain.product;

import com.loopers.domain.product.attribute.ProductSearchSortType;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProductQueryCommand {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class SearchProducts {
        private final String keyword;
        private final Long brandId;
        private final ProductSearchSortType sort;

        private final Integer page;
        private final Integer size;
    }

}
