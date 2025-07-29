package com.loopers.domain.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public OrderResult.Create create(OrderCommand.Create command) {
        UUID id = orderRepository.findNextOrderId();

        Order order = Order.builder()
                .id(id)
                .totalPrice(command.getTotalPrice())
                .userId(command.getUserId())
                .build();

        List<OrderProduct> products = command.getProducts()
                .stream()
                .map(product -> OrderProduct.builder()
                        .price(product.getPrice())
                        .quantity(product.getQuantity())
                        .orderId(order.getId())
                        .productOptionId(product.getProductOptionId())
                        .build())
                .toList();
        order.addProducts(products);

        orderRepository.saveOrder(order);

        return OrderResult.Create.from(order);
    }

}
