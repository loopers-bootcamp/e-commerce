package com.loopers.application.ranking;

import com.loopers.domain.brand.BrandResult;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.product.ProductResult;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.ranking.RankingCommand;
import com.loopers.domain.ranking.RankingResult;
import com.loopers.domain.ranking.RankingService;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RankingFacade {

    private final RankingService rankingService;
    private final ProductService productService;
    private final BrandService brandService;

    public int saveDailyRankings(LocalDate date) {
        // TODO: carry-over
        RankingResult.FindRanks rankResult = rankingService.findRanks(date);
        if (CollectionUtils.isEmpty(rankResult.items())) {
            return 0;
        }

        int maxRanks = Math.min(rankResult.items().size(), 1000);
        List<RankingResult.FindRanks.Item> rankItems = rankResult.items().subList(0, maxRanks);

        List<RankingCommand.SaveRankings.Item> items = new ArrayList<>();
        for (RankingResult.FindRanks.Item rankItem : rankItems) {
            ProductResult.GetProductDetail detail = productService.getProductDetail(rankItem.productId())
                    .orElseThrow(() -> new BusinessException(CommonErrorType.NOT_FOUND));
            String brandName = brandService.getBrand(detail.brandId())
                    .map(BrandResult.GetBrand::getBrandName)
                    .orElse(null);

            RankingCommand.SaveRankings.Item item = RankingCommand.SaveRankings.Item.builder()
                    .productId(detail.productId())
                    .productName(detail.productName())
                    .basePrice(detail.basePrice())
                    .likeCount(detail.likeCount())
                    .brandId(detail.brandId())
                    .brandName(brandName)
                    .build();
            items.add(item);
        }

        RankingCommand.SaveRankings saveCommand = RankingCommand.SaveRankings.builder()
                .date(date)
                .items(List.copyOf(items))
                .build();
        rankingService.saveRankings(saveCommand);

        return items.size();
    }

}
