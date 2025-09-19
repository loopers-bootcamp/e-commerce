package com.loopers.interfaces.api.ranking;

import com.loopers.application.ranking.RankingOutput;
import lombok.*;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RankingResponse {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class SearchDaily {
        private final Integer totalPages;
        private final Long totalItems;
        private final Integer page;
        private final Integer size;
        private final List<Item> items;

        public static SearchDaily from(RankingOutput.SearchDaily output) {
            return builder()
                    .totalPages(output.totalPages())
                    .totalItems(output.totalItems())
                    .page(output.page())
                    .size(output.size())
                    .items(output.items().stream().map(Item::from).toList())
                    .build();
        }
    }

    // -------------------------------------------------------------------------------------------------

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class SearchWeekly {
        private final Integer totalPages;
        private final Long totalItems;
        private final Integer page;
        private final Integer size;
        private final List<Item> items;

        public static SearchWeekly from(RankingOutput.SearchWeekly output) {
            return builder()
                    .totalPages(output.totalPages())
                    .totalItems(output.totalItems())
                    .page(output.page())
                    .size(output.size())
                    .items(output.items().stream().map(Item::from).toList())
                    .build();
        }
    }

    // -------------------------------------------------------------------------------------------------

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class SearchMonthly {
        private final Integer totalPages;
        private final Long totalItems;
        private final Integer page;
        private final Integer size;
        private final List<Item> items;

        public static SearchMonthly from(RankingOutput.SearchMonthly output) {
            return builder()
                    .totalPages(output.totalPages())
                    .totalItems(output.totalItems())
                    .page(output.page())
                    .size(output.size())
                    .items(output.items().stream().map(Item::from).toList())
                    .build();
        }
    }

    // -------------------------------------------------------------------------------------------------

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

        public static Item from(RankingOutput.Item output) {
            return builder()
                    .productId(output.productId())
                    .productName(output.productName())
                    .basePrice(output.basePrice())
                    .likeCount(output.likeCount())
                    .brandId(output.brandId())
                    .brandName(output.brandName())
                    .build();
        }
    }

}
