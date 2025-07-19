package com.loopers.domain.point;

import lombok.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PointResult {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class GetPoint {
        private final Long pointId;
        private final Long balance;
        private final Long userId;

        public static GetPoint from(Point point) {
            return GetPoint.builder()
                    .pointId(point.getId())
                    .balance(point.getBalance())
                    .userId(point.getUserId())
                    .build();
        }
    }

    // -------------------------------------------------------------------------------------------------

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Create {
        private final Long pointId;
        private final Long balance;
        private final Long userId;

        public static Create from(Point point) {
            return Create.builder()
                    .pointId(point.getId())
                    .balance(point.getBalance())
                    .userId(point.getUserId())
                    .build();
        }
    }

    // -------------------------------------------------------------------------------------------------

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Increase {
        private final Long pointId;
        private final Long balance;
        private final Long userId;

        public static Increase from(Point point) {
            return Increase.builder()
                    .pointId(point.getId())
                    .balance(point.getBalance())
                    .userId(point.getUserId())
                    .build();
        }
    }

    // -------------------------------------------------------------------------------------------------

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Decrease {
        private final Long pointId;
        private final Long balance;
        private final Long userId;

        public static Decrease from(Point point) {
            return Decrease.builder()
                    .pointId(point.getId())
                    .balance(point.getBalance())
                    .userId(point.getUserId())
                    .build();
        }
    }

}
