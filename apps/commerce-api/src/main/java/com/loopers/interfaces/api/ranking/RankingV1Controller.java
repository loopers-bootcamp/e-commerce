package com.loopers.interfaces.api.ranking;

import com.loopers.application.product.ProductFacade;
import com.loopers.domain.product.ProductService;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/rankings")
public class RankingV1Controller implements RankingV1ApiSpec {

    private final ProductFacade productFacade;
    private final ProductService productService;

    @GetMapping
    @Override
    public ApiResponse<RankingResponse.SearchRankings> searchRankings(
            RankingRequest.SearchRankings request
    ) {
        return null;
    }

}
