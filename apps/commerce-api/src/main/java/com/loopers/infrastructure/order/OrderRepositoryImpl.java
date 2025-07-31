package com.loopers.infrastructure.order;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedEpochRandomGenerator;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderProduct;
import com.loopers.domain.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private static final TimeBasedEpochRandomGenerator generator = Generators.timeBasedEpochRandomGenerator();

    private final OrderJpaRepository orderJpaRepository;
    private final OrderProductJpaRepository orderProductJpaRepository;

    @Override
    public Optional<Order> findOrderDetailById(UUID orderId) {
        return orderJpaRepository.findById(orderId)
                .stream()
                .peek(order -> {
                    List<OrderProduct> products = orderProductJpaRepository.findByOrderId(order.getId());
                    order.addProducts(products);
                })
                .findAny();
    }

    @Override
    public Optional<Order> findByIdForUpdate(UUID orderId) {
        return orderJpaRepository.findById(orderId);
    }

    @Override
    public UUID findNextOrderId() {
        return generator.generate();
    }

    @Override
    public Order save(Order order) {
        Order savedOrder = orderJpaRepository.save(order);
        orderProductJpaRepository.saveAll(order.getProducts());

        return savedOrder;
    }

}
