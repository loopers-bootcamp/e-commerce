package com.loopers.application.activity;

import lombok.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ActivityInput {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Like {
        private final String userName;
        private final Long productId;
    }

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Dislike {
        private final String userName;
        private final Long productId;
    }

}
