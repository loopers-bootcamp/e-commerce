package com.loopers.domain.product.event;

public interface ProductEventPublisher {

    void publishEvent(ProductEvent.LikeChanged event);

    void publishEvent(ProductEvent.StockChanged event);

    void publishEvent(ProductEvent.Sale event);

}
