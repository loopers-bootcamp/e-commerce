package com.loopers.interfaces.api.order;

import jakarta.validation.constraints.NotEmpty;
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

        @Getter
        @Builder
        @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Product {
            private final Long productOptionId;
            private final Integer quantity;
        }
    }

}
