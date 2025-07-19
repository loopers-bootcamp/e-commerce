package com.loopers.interfaces.api.point;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PointRequest {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Charge {
        @NotNull
        private final Long amount;
    }

}
