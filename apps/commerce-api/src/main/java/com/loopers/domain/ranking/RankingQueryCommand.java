package com.loopers.domain.ranking;

import java.util.List;

public record RankingQueryCommand() {

    public record SaveRankings(
            Long productId,
            String productName,
            Integer basePrice,
            Long likeCount,
            Long brandId,
            String brandName
    ) {
        public static List<SaveRankings> from(RankingCommand.SaveRankings command) {
            return command.getItems()
                    .stream()
                    .map(item -> new SaveRankings(
                            item.getProductId(),
                            item.getProductName(),
                            item.getBasePrice(),
                            item.getLikeCount(),
                            item.getBrandId(),
                            item.getBrandName()
                    ))
                    .toList();
        }
    }

}
