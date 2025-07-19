package com.loopers.application.point;

import lombok.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PointInput {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Charge {
        private final String userName;
        private final Long amount;
    }

}
