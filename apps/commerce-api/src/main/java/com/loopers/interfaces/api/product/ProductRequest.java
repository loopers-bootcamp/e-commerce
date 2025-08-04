package com.loopers.interfaces.api.product;

import com.loopers.domain.product.attribute.ProductSearchSortType;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProductRequest {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class SearchProducts {
        @Nullable
        private final String keyword;
        @Positive
        private final Long brandId;
        private final ProductSearchSortType sort;

        @NotNull
        @PositiveOrZero
        private final Integer page;
        @NotNull
        @Positive
        private final Integer size;
    }

}
