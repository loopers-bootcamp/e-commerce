package com.loopers.domain.brand;

import lombok.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BrandResult {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class GetBrand {
        private final Long brandId;
        private final String brandName;
        private final String brandDescription;

        public static GetBrand from(Brand brand) {
            return GetBrand.builder()
                    .brandId(brand.getId())
                    .brandName(brand.getName())
                    .brandDescription(brand.getDescription())
                    .build();
        }
    }

}
