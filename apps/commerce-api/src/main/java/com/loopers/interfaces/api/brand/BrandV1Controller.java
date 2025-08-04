package com.loopers.interfaces.api.brand;

import com.loopers.domain.brand.BrandResult;
import com.loopers.domain.brand.BrandService;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/brands")
public class BrandV1Controller implements BrandV1ApiSpec {

    private final BrandService brandService;

    @GetMapping("/{brandId}")
    @Override
    public ApiResponse<BrandResponse.GetBrand> getBrand(
            @PathVariable Long brandId
    ) {
        BrandResult.GetBrand brand = brandService.getBrand(brandId)
                .orElseThrow(() -> new BusinessException(CommonErrorType.NOT_FOUND));
        BrandResponse.GetBrand response = BrandResponse.GetBrand.from(brand);

        return ApiResponse.success(response);
    }

}
