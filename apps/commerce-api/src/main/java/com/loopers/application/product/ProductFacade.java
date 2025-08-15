package com.loopers.application.product;

import com.loopers.domain.activity.ActivityCommand;
import com.loopers.domain.activity.ActivityService;
import com.loopers.domain.brand.BrandResult;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.product.ProductResult;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserResult;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class ProductFacade {

    private final ProductService productService;
    private final BrandService brandService;
    private final UserService userService;
    private final ActivityService activityService;

    @Qualifier(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME)
    private final SimpleAsyncTaskExecutor taskExecutor;

    public ProductOutput.GetProductDetail getProductDetail(ProductInput.GetProductDetail input) {
        Long productId = input.getProductId();

        ProductResult.GetProductDetail detail = productService.getProductDetail(productId)
                .orElseThrow(() -> new BusinessException(CommonErrorType.NOT_FOUND));

        BrandResult.GetBrand brand = brandService.getBrand(detail.getBrandId()).orElse(null);

        // 회원이면 비동기로 조회수를 증가한다.
        String userName = input.getUserName();
        if (StringUtils.hasText(userName)) {
            taskExecutor.execute(() -> {
                UserResult.GetUser userResult = userService.getUser(userName)
                        .orElseThrow(() -> new BusinessException(CommonErrorType.NOT_FOUND));

                ActivityCommand.View activityCommand = ActivityCommand.View.builder()
                        .userId(userResult.getUserId())
                        .productId(productId)
                        .build();

                activityService.view(activityCommand);
            });
        }

        return ProductOutput.GetProductDetail.from(detail, brand);
    }

}
