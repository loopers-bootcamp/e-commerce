package com.loopers.infrastructure.order;

import com.loopers.domain.order.ExternalOrderSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class ExternalOrderSenderImpl implements ExternalOrderSender {

    @Override
    public void sendOrder(UUID orderId) {
        log.info("Order is completed: (orderId={})", orderId);
    }

}
