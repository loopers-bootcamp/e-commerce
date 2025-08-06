package com.loopers.interfaces.api.order;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrderRequest {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Create {
        @NotEmpty
        @Size(max = 100)
        private final List<Product> products;
        @Size(max = 10)
        private final List<@NotNull @Positive Long> userCouponIds;

        @Getter
        @Builder
        @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Product {
            private final Long productOptionId;
            private final Integer quantity;
        }
    }

}
