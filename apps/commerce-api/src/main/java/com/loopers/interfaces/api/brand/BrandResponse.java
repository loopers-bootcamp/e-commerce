package com.loopers.interfaces.api.brand;

import com.loopers.domain.brand.BrandResult;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BrandResponse {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class GetBrand {
        private final Long brandId;
        private final String brandName;
        private final String brandDescription;

        public static GetBrand from(BrandResult.GetBrand result) {
            return builder()
                    .brandId(result.getBrandId())
                    .brandName(result.getBrandName())
                    .brandDescription(result.getBrandDescription())
                    .build();
        }
    }

}
