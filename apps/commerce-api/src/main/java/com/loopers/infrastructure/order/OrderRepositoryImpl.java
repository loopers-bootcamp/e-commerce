package com.loopers.infrastructure.order;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedEpochRandomGenerator;
import com.loopers.domain.order.*;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private static final TimeBasedEpochRandomGenerator generator = Generators.timeBasedEpochRandomGenerator();

    private final OrderJpaRepository orderJpaRepository;
    private final OrderProductJpaRepository orderProductJpaRepository;
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<Order> findOrderDetailById(UUID orderId) {
        QOrder o = QOrder.order;
        QOrderProduct op = QOrderProduct.orderProduct;

        List<Tuple> rows = jpaQueryFactory
                .select(o, op)
                .from(o)
                .join(op).on(op.orderId.eq(o.id))
                .where(o.id.eq(orderId))
                .fetch();

        if (CollectionUtils.isEmpty(rows)) {
            return Optional.empty();
        }

        Order order = rows.getFirst().get(o);
        List<OrderProduct> products = rows.stream()
                .map(row -> row.get(op))
                .toList();
        order.addProducts(products);

        return Optional.of(order);
    }

    @Override
    public Optional<Order> findOneForUpdate(UUID orderId) {
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
