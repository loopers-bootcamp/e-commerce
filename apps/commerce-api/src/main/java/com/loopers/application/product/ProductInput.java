package com.loopers.application.product;

import lombok.*;
import org.jetbrains.annotations.Nullable;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProductInput {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class GetProductDetail {
        private final Long productId;
        @Nullable
        private final String userName;
    }

}
