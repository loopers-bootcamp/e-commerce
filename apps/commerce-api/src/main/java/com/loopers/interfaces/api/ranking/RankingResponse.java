package com.loopers.interfaces.api.ranking;

import com.loopers.application.ranking.RankingOutput;
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

        public static SearchRankings from(RankingOutput.SearchRankings output) {
            return builder()
                    .totalPages(output.totalPages())
                    .totalItems(output.totalItems())
                    .page(output.page())
                    .size(output.size())
                    .items(output.items()
                            .stream()
                            .map(content -> Item.builder()
                                    .productId(content.productId())
                                    .productName(content.productName())
                                    .basePrice(content.basePrice())
                                    .likeCount(content.likeCount())
                                    .brandId(content.brandId())
                                    .brandName(content.brandName())
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
