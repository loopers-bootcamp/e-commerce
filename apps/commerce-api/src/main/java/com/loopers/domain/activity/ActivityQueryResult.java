package com.loopers.domain.activity;

import lombok.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ActivityQueryResult {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class GetLikedProducts {
        private final Long likedProductId;
        private final Long userId;
        private final Long productId;
        private final String productName;
    }

}
