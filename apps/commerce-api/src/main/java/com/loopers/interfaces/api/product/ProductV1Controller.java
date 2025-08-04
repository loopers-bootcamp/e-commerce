package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductFacade;
import com.loopers.application.product.ProductInput;
import com.loopers.application.product.ProductOutput;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductResult;
import com.loopers.domain.product.ProductService;
import com.loopers.interfaces.api.ApiHeader;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductV1Controller implements ProductV1ApiSpec {

    private final ProductFacade productFacade;
    private final ProductService productService;

    @GetMapping
    @Override
    public ApiResponse<ProductResponse.SearchProducts> searchProducts(
            ProductRequest.SearchProducts request
    ) {
        ProductCommand.SearchProducts command = ProductCommand.SearchProducts.builder()
                .keyword(request.getKeyword())
                .brandId(request.getBrandId())
                .sort(request.getSort())
                .page(request.getPage())
                .size(request.getSize())
                .build();

        ProductResult.SearchProducts products = productService.searchProducts(command);
        ProductResponse.SearchProducts response = ProductResponse.SearchProducts.from(products);

        return ApiResponse.success(response);
    }

    @GetMapping("/{productId}")
    @Override
    public ApiResponse<ProductResponse.GetProductDetail> getProductDetail(
            @RequestHeader(name = ApiHeader.USER_ID, required = false)
            String userName,
            @PathVariable
            Long productId
    ) {
        ProductInput.GetProductDetail input = ProductInput.GetProductDetail.builder()
                .userName(userName)
                .productId(productId)
                .build();

        ProductOutput.GetProductDetail productDetail = productFacade.getProductDetail(input);
        ProductResponse.GetProductDetail response = ProductResponse.GetProductDetail.from(productDetail);

        return ApiResponse.success(response);
    }

}
