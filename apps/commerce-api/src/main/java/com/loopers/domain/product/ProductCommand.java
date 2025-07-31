package com.loopers.domain.product;

import com.loopers.domain.product.attribute.ProductSearchSortType;
import lombok.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProductCommand {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class SearchProducts {
        @Nullable
        private final String keyword;
        @Nullable
        private final Long brandId;
        private final ProductSearchSortType sortType;

        private final Integer page;
        private final Integer size;
    }

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class AddStocks {
        private final List<Item> items;

        @Getter
        @Builder
        @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Item {
            private final Long productOptionId;
            private final Integer amount;
        }
    }

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class DeductStocks {
        private final List<Item> items;

        @Getter
        @Builder
        @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Item {
            private final Long productOptionId;
            private final Integer amount;
        }
    }

}
