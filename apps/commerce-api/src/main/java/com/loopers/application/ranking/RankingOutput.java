package com.loopers.application.ranking;

import com.loopers.domain.product.ProductResult;
import com.loopers.domain.ranking.RankingResult;

import java.util.List;

public record RankingOutput() {

    public record SearchDaily(
            Integer totalPages,
            Long totalItems,
            Integer page,
            Integer size,
            List<Item> items
    ) {
        public static SearchDaily empty(RankingResult.SearchDaily result) {
            return new SearchDaily(result.totalPages(), result.totalItems(), result.page(), result.size(), List.of());
        }

        public static SearchDaily from(RankingResult.SearchDaily ranks, List<ProductResult.GetProductDetail> details) {
            return new SearchDaily(
                    ranks.totalPages(),
                    ranks.totalItems(),
                    ranks.page(),
                    ranks.size(),
                    details.stream().map(Item::from).toList()
            );
        }
    }

    // -------------------------------------------------------------------------------------------------

    public record SearchWeekly(
            Integer totalPages,
            Long totalItems,
            Integer page,
            Integer size,
            List<Item> items
    ) {
        public static SearchWeekly empty(RankingResult.SearchWeekly result) {
            return new SearchWeekly(result.totalPages(), result.totalItems(), result.page(), result.size(), List.of());
        }

        public static SearchWeekly from(RankingResult.SearchWeekly ranks, List<ProductResult.GetProductDetail> details) {
            return new SearchWeekly(
                    ranks.totalPages(),
                    ranks.totalItems(),
                    ranks.page(),
                    ranks.size(),
                    details.stream().map(Item::from).toList()
            );
        }
    }

    // -------------------------------------------------------------------------------------------------

    public record SearchMonthly(
            Integer totalPages,
            Long totalItems,
            Integer page,
            Integer size,
            List<Item> items
    ) {
        public static SearchMonthly empty(RankingResult.SearchMonthly result) {
            return new SearchMonthly(result.totalPages(), result.totalItems(), result.page(), result.size(), List.of());
        }

        public static SearchMonthly from(RankingResult.SearchMonthly ranks, List<ProductResult.GetProductDetail> details) {
            return new SearchMonthly(
                    ranks.totalPages(),
                    ranks.totalItems(),
                    ranks.page(),
                    ranks.size(),
                    details.stream().map(Item::from).toList()
            );
        }
    }

    // -------------------------------------------------------------------------------------------------

    public record Item(
            Long productId,
            String productName,
            Integer basePrice,
            Long likeCount,
            Long brandId,
            String brandName
    ) {
        public static Item from(ProductResult.GetProductDetail detail) {
            return new Item(
                    detail.productId(),
                    detail.productName(),
                    detail.basePrice(),
                    detail.likeCount(),
                    detail.brandId(),
                    detail.brandName()
            );
        }
    }

}
