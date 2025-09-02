package com.loopers.domain.product.event;

public interface ProductEventPublisher {

    void publishEvent(ProductEvent.LikeChanged event);

}
