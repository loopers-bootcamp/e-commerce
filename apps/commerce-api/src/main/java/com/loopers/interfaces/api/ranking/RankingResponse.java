package com.loopers.interfaces.api.ranking;

import com.loopers.domain.product.ProductResult;
import lombok.*;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RankingResponse {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class SearchRankings {
        private final Integer totalPages;
        private final Long totalItems;
        private final Integer page;
        private final Integer size;
        private final List<Item> items;

        public static SearchRankings from(ProductResult.SearchProducts result) {
            return builder()
                    .totalPages(result.getTotalPages())
                    .totalItems(result.getTotalItems())
                    .page(result.getPage())
                    .size(result.getSize())
                    .items(result.getItems()
                            .stream()
                            .map(content -> Item.builder()
                                    .productId(content.getProductId())
                                    .productName(content.getProductName())
                                    .basePrice(content.getBasePrice())
                                    .likeCount(content.getLikeCount())
                                    .brandId(content.getBrandId())
                                    .brandName(content.getBrandName())
                                    .build()
                            )
                            .toList()
                    )
                    .build();
        }

        @Getter
        @Builder
        @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Item {
            private final Long productId;
            private final String productName;
            private final Integer basePrice;
            private final Long likeCount;
            private final Long brandId;
            private final String brandName;
        }
    }

}
