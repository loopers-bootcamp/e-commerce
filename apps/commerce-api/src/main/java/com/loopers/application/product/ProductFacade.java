package com.loopers.application.product;

import com.loopers.domain.activity.event.ActivityEvent;
import com.loopers.domain.brand.BrandResult;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.product.ProductResult;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductFacade {

    private final ProductService productService;
    private final BrandService brandService;
    private final UserService userService;

    @Qualifier(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME)
    private final TaskExecutor taskExecutor;
    private final ApplicationEventPublisher eventPublisher;

    public ProductOutput.GetProductDetail getProductDetail(ProductInput.GetProductDetail input) {
        Long productId = input.getProductId();

        ProductResult.GetProductDetail detail = productService.getProductDetail(productId)
                .orElseThrow(() -> new BusinessException(CommonErrorType.NOT_FOUND));

        BrandResult.GetBrand brand = brandService.getBrand(detail.brandId()).orElse(null);

        // 회원이면 상품 조회 이벤트를 발행한다.
        Optional.ofNullable(input.getUserName())
                .filter(StringUtils::hasText)
                .ifPresent(userName -> taskExecutor.execute(() ->
                        userService.getUser(userName)
                                .ifPresent(user -> eventPublisher.publishEvent(
                                        ActivityEvent.View.from(user.getUserId(), productId)
                                ))
                ));

        return ProductOutput.GetProductDetail.from(detail, brand);
    }

}
