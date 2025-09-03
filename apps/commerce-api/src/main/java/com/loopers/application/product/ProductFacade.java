package com.loopers.application.product;

import com.loopers.domain.activity.event.ActivityEvent;
import com.loopers.domain.brand.BrandResult;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.product.ProductResult;
import com.loopers.domain.product.ProductService;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class ProductFacade {

    private final ProductService productService;
    private final BrandService brandService;
    private final ApplicationEventPublisher eventPublisher;

    public ProductOutput.GetProductDetail getProductDetail(ProductInput.GetProductDetail input) {
        Long productId = input.getProductId();

        ProductResult.GetProductDetail detail = productService.getProductDetail(productId)
                .orElseThrow(() -> new BusinessException(CommonErrorType.NOT_FOUND));

        BrandResult.GetBrand brand = brandService.getBrand(detail.brandId()).orElse(null);

        // 회원이면 상품 조회 이벤트를 발행한다.
        String userName = input.getUserName();
        if (StringUtils.hasText(userName)) {
            eventPublisher.publishEvent(ActivityEvent.View.from(userName, productId));
        }

        return ProductOutput.GetProductDetail.from(detail, brand);
    }

}
