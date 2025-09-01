package com.loopers.domain.order;

import java.util.UUID;

public interface ExternalOrderSender {

    void sendOrder(UUID orderId);

}
