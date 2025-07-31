package com.loopers.domain.product;

import com.loopers.annotation.ReadOnlyTransactional;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @ReadOnlyTransactional
    public Optional<ProductResult.GetProductDetail> getProductDetail(Long productId) {
        if (productId == null) {
            return Optional.empty();
        }

        return productRepository.findProductDetailById(productId)
                .map(ProductResult.GetProductDetail::from);
    }

    @ReadOnlyTransactional
    public Optional<ProductResult.GetProductOptions> getProductOptions(List<Long> productOptionIds) {
        if (CollectionUtils.isEmpty(productOptionIds)) {
            return Optional.empty();
        }

        return productRepository.findProductOptionsByIds(productOptionIds)
                .map(ProductResult.GetProductOptions::from);
    }

    @Transactional
    public void addStocks(ProductCommand.AddStocks command) {
        List<ProductCommand.AddStocks.Item> items = command.getItems();
        if (CollectionUtils.isEmpty(items)) {
            return;
        }

        if (items.stream().map(ProductCommand.AddStocks.Item::getProductOptionId).distinct().count() != items.size()) {
            throw new BusinessException(CommonErrorType.INVALID, "중복된 상품 옵션 아이디가 있습니다.");
        }

        List<Long> productOptionIds = items.stream().map(ProductCommand.AddStocks.Item::getProductOptionId).toList();
        Map<Long, Stock> stockMap = productRepository.findStocksByProductOptionIdsForUpdate(productOptionIds)
                .stream().collect(toMap(Stock::getProductOptionId, Function.identity()));

        if (stockMap.size() != productOptionIds.size()) {
            throw new BusinessException(CommonErrorType.NOT_FOUND, "상품 재고를 찾을 수 없습니다.");
        }

        for (ProductCommand.AddStocks.Item item : items) {
            Stock stock = stockMap.get(item.getProductOptionId());
            stock.add(item.getAmount());
        }

        productRepository.saveStocks(List.copyOf(stockMap.values()));
    }

    @Transactional
    public void deductStocks(ProductCommand.DeductStocks command) {
        List<ProductCommand.DeductStocks.Item> items = command.getItems();
        if (CollectionUtils.isEmpty(items)) {
            return;
        }

        if (items.stream().map(ProductCommand.DeductStocks.Item::getProductOptionId).distinct().count() != items.size()) {
            throw new BusinessException(CommonErrorType.INVALID, "중복된 상품 옵션 아이디가 있습니다.");
        }

        List<Long> productOptionIds = items.stream().map(ProductCommand.DeductStocks.Item::getProductOptionId).toList();
        Map<Long, Stock> stockMap = productRepository.findStocksByProductOptionIdsForUpdate(productOptionIds)
                .stream().collect(toMap(Stock::getProductOptionId, Function.identity()));

        if (stockMap.size() != productOptionIds.size()) {
            throw new BusinessException(CommonErrorType.NOT_FOUND, "상품 재고를 찾을 수 없습니다.");
        }

        for (ProductCommand.DeductStocks.Item item : items) {
            Stock stock = stockMap.get(item.getProductOptionId());
            stock.deduct(item.getAmount());
        }

        productRepository.saveStocks(List.copyOf(stockMap.values()));
    }

}
