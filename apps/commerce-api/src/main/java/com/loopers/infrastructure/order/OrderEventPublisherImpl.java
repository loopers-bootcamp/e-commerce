package com.loopers.infrastructure.order;

import com.loopers.domain.order.OrderEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class OrderEventPublisherImpl implements OrderEventPublisher {

    @Override
    public void complete(UUID orderId) {
        log.info("Order is completed: (orderId={})", orderId);
    }

}
