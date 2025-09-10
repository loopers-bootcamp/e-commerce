package com.loopers.domain.ranking;

import lombok.*;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RankingCommand {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class SearchRankings {
        private final LocalDate date;
        private final Integer page;
        private final Integer size;
    }

    // -------------------------------------------------------------------------------------------------

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class SaveRankings {
        private final LocalDate date;
        private final List<Item> items;

        @Getter
        @Builder
        @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Item {
            private final Long productId;
            private final String productName;
            private final Integer basePrice;
            private final Long likeCount;
            @Nullable
            private final Long brandId;
            @Nullable
            private final String brandName;
        }
    }

}
