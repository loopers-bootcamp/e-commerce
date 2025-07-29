package com.loopers.domain.activity;

import lombok.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ActivityCommand {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Like {
        private final Long userId;
        private final Long productId;
    }

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class View {
        private final Long userId;
        private final Long productId;
    }

}
