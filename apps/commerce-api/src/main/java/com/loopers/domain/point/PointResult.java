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
    public static class Charge {
        private final Long pointId;
        private final Long balance;
        private final Long userId;

        public static Charge from(Point point) {
            return Charge.builder()
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
    public static class Spend {
        private final Long pointId;
        private final Long balance;
        private final Long userId;

        public static Spend from(Point point) {
            return Spend.builder()
                    .pointId(point.getId())
                    .balance(point.getBalance())
                    .userId(point.getUserId())
                    .build();
        }
    }

}
