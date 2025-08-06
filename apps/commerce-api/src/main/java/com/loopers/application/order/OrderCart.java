package com.loopers.application.order;

import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.product.ProductResult;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderCart {

    private final Map<Long, OrderInput.Create.Product> cartMap;
    private final Map<Long, ProductResult.GetProductOptions.Item> optionMap;
    private final List<Long> userCouponIds;

    public static OrderCart from(OrderInput.Create input, ProductResult.GetProductOptions options) {
        Map<Long, OrderInput.Create.Product> cartMap = input.getProducts()
                .stream().collect(collectingAndThen(
                        toMap(OrderInput.Create.Product::getProductOptionId, Function.identity()),
                        Collections::unmodifiableMap
                ));
        Map<Long, ProductResult.GetProductOptions.Item> optionMap = options.getItems()
                .stream().collect(collectingAndThen(
                        toMap(ProductResult.GetProductOptions.Item::getProductOptionId, Function.identity()),
                        Collections::unmodifiableMap
                ));

        if (cartMap.size() != optionMap.size()) {
            throw new BusinessException(CommonErrorType.NOT_FOUND);
        }

        return new OrderCart(cartMap, optionMap, input.getUserCouponIds());
    }

    public boolean isEnoughStock() {
        for (Map.Entry<Long, OrderInput.Create.Product> e : this.cartMap.entrySet()) {
            Integer buyQuantity = e.getValue().getQuantity();
            Integer stockQuantity = this.optionMap.get(e.getKey()).getStockQuantity();

            if (buyQuantity > stockQuantity) {
                return false;
            }
        }

        return true;
    }

    public boolean isEnoughPoint(long balance) {
        return balance >= getTotalPrice();
    }

    public long getTotalPrice() {
        long totalPrice = 0;
        for (Map.Entry<Long, OrderInput.Create.Product> e : this.cartMap.entrySet()) {
            int buyQuantity = e.getValue().getQuantity();
            int salePrice = this.optionMap.get(e.getKey()).getSalePrice();

            totalPrice += (long) buyQuantity * salePrice;
        }

        return totalPrice;
    }

    public OrderCommand.Create toCommand(Long userId, Integer discountAmount) {
        List<OrderCommand.Create.Product> products = new ArrayList<>();

        for (Map.Entry<Long, OrderInput.Create.Product> e : this.cartMap.entrySet()) {
            Long productOptionId = e.getKey();
            Integer buyQuantity = e.getValue().getQuantity();
            Integer salePrice = this.optionMap.get(productOptionId).getSalePrice();

            OrderCommand.Create.Product product = OrderCommand.Create.Product.builder()
                    .productOptionId(productOptionId)
                    .quantity(buyQuantity)
                    .price(salePrice)
                    .build();

            products.add(product);
        }

        return OrderCommand.Create.builder()
                .userId(userId)
                .totalPrice(getTotalPrice())
                .discountAmount(discountAmount)
                .products(List.copyOf(products))
                .userCouponIds(List.copyOf(this.userCouponIds))
                .build();
    }

}
