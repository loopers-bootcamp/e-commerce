package com.loopers.application.point;

import com.loopers.domain.point.PointResult;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PointOutput {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class GetPoint {
        private final Long pointId;
        private final Long balance;
        private final Long userId;

        public static GetPoint from(PointResult.GetPoint result) {
            return GetPoint.builder()
                    .pointId(result.getPointId())
                    .balance(result.getBalance())
                    .userId(result.getUserId())
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

        public static Charge from(PointResult.Increase result) {
            return Charge.builder()
                    .pointId(result.getUserId())
                    .balance(result.getBalance())
                    .userId(result.getUserId())
                    .build();
        }
    }

}
