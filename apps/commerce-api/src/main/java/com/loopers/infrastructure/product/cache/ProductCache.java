package com.loopers.infrastructure.product.cache;

import lombok.experimental.FieldNameConstants;

import java.util.List;

public class ProductCache {

    public static class Simple {
        @FieldNameConstants(asEnum = true)
        public record V1(
                Long productId,
                String productName,
                Integer basePrice,
                Long likeCount,
                Long brandId,
                String brandName
        ) {
            public static V1 from(List<Object> row) {
                Long productId = row.get(0) == null ? null : Long.parseLong(row.get(0).toString());
                String productName = row.get(1) == null ? null : String.valueOf(row.get(1));
                Integer basePrice = row.get(2) == null ? null : Integer.parseInt(row.get(2).toString());
                Long likeCount = row.get(3) == null ? null : Long.parseLong(row.get(3).toString());
                Long brandId = row.get(4) == null ? null : Long.parseLong(row.get(4).toString());
                String brandName = row.get(5) == null ? null : String.valueOf(row.get(5));

                return new ProductCache.Simple.V1(
                        productId,
                        productName,
                        basePrice,
                        likeCount,
                        brandId,
                        brandName
                );
            }
        }
    }

}
