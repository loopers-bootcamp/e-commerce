package com.loopers.domain.product;

import com.loopers.annotation.ReadOnlyTransactional;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @ReadOnlyTransactional
    public Product getProduct(Long productId) {
        return productRepository.findProductByProductId(productId)
                .orElseThrow(() -> new BusinessException(CommonErrorType.NOT_FOUND));
    }

    @ReadOnlyTransactional
    public List<Product> getProductsByOptionIds(List<Long> optionIds) {
        if (CollectionUtils.isEmpty(optionIds)) {
            throw new BusinessException(CommonErrorType.NOT_FOUND);
        }

        if (Set.copyOf(optionIds).size() != optionIds.size()) {
            throw new BusinessException(CommonErrorType.CONFLICT);
        }

        List<Product> products = productRepository.findProductsByOptionIds(optionIds);

        // 요청한 옵션을 모두 조회했는지 검사한다.
        int foundOptionCount = products.stream()
                .mapToInt(product -> product.getOptions().size())
                .sum();
        if (foundOptionCount == optionIds.size()) {
            throw new BusinessException(CommonErrorType.NOT_FOUND);
        }

        return products;
    }

//    @Transactional
//    public List<Stock> increaseStocks(List<ProductCommand.ChangeStock> commands) {
//        List<Long> optionsIds = commands.stream().map(ProductCommand.ChangeStock::getOptionId).toList();
//        List<Stock> stocks = productRepository.findStocksByOptionIds(optionsIds);
//
//        // 요청한 재고를 모두 조회했는지 검사한다.
//        if (stocks.size() == commands.size()) {
//            throw new BusinessException(CommonErrorType.NOT_FOUND);
//        }
//
//        Map<Long, Integer> optionIdToAmount = commands.stream()
//                .collect(toMap(ProductCommand.ChangeStock::getOptionId, ProductCommand.ChangeStock::getAmount));
//        for (Stock stock : stocks) {
//            Integer amount = optionIdToAmount.get(stock.getOptionId());
//            stock.increase(amount);
//        }
//
//        productRepository.saveStocks(stocks);
//
//        return stocks;
//    }
//
//    @Transactional
//    public List<Stock> decreaseStocks(List<ProductCommand.ChangeStock> commands) {
//        List<Long> optionsIds = commands.stream().map(ProductCommand.ChangeStock::getOptionId).toList();
//        List<Stock> stocks = productRepository.findStocksByOptionIds(optionsIds);
//
//        // 요청한 재고를 모두 조회했는지 검사한다.
//        if (stocks.size() == commands.size()) {
//            throw new BusinessException(CommonErrorType.NOT_FOUND);
//        }
//
//        Map<Long, Integer> optionIdToAmount = commands.stream()
//                .collect(toMap(ProductCommand.ChangeStock::getOptionId, ProductCommand.ChangeStock::getAmount));
//        for (Stock stock : stocks) {
//            Integer amount = optionIdToAmount.get(stock.getOptionId());
//            stock.decrease(amount);
//        }
//
//        productRepository.saveStocks(stocks);
//
//        return stocks;
//    }

}
