package com.loopers.interfaces.api.point;

import com.loopers.application.point.PointOutput;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PointResponse {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class GetPoint {
        private final Long balance;

        public static GetPoint from(PointOutput.GetPoint output) {
            return GetPoint.builder()
                    .balance(output.getBalance())
                    .build();
        }
    }

    // -------------------------------------------------------------------------------------------------

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Charge {
        private final Long balance;

        public static Charge from(PointOutput.Charge output) {
            return Charge.builder()
                    .balance(output.getBalance())
                    .build();
        }
    }

}
