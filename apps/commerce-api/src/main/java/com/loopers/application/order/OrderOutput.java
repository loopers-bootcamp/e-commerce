package com.loopers.application.order;

import com.loopers.domain.order.OrderResult;
import com.loopers.domain.order.attribute.OrderStatus;
import lombok.*;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrderOutput {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Create {
        private final UUID orderId;
        private final Long totalPrice;
        private final OrderStatus status;

        public static Create from(OrderResult.Create result) {
            return Create.builder()
                    .orderId(result.getOrderId())
                    .totalPrice(result.getTotalPrice())
                    .status(result.getStatus())
                    .build();
        }
    }

}
