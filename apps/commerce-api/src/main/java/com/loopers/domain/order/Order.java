package com.loopers.domain.order;

import com.loopers.config.jpa.converter.OrderIdConverter;
import com.loopers.config.jpa.converter.OrderStatusConverter;
import com.loopers.domain.BaseEntity;
import com.loopers.domain.order.attribute.OrderId;
import com.loopers.domain.order.attribute.OrderStatus;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.*;

@Getter
@Entity
@Table(name = "order", indexes = {
        @Index(name = "idx__order__ref_user_id", columnList = "userId"),
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    /**
     * 아이디
     */
    @Id
    @Convert(converter = OrderIdConverter.class)
    @Column(name = "order_id", nullable = false, updatable = false)
    private OrderId id;

    /**
     * 총 가격
     */
    @Column(name = "total_price", nullable = false)
    private Long totalPrice;

    /**
     * 주문 상태
     */
    @Convert(converter = OrderStatusConverter.class)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    // -------------------------------------------------------------------------------------------------

    /**
     * 사용자 아이디
     */
    @Column(name = "ref_user_id", nullable = false, updatable = false)
    private Long userId;

    /**
     * 상품 아이디
     */
    @Column(name = "ref_product_id", nullable = false, updatable = false)
    private Long productId;

    // -------------------------------------------------------------------------------------------------

    /**
     * 주문 상품 옵션 목록
     */
    @Transient
    private List<OrderProductOption> productOptions = Collections.emptyList();

    // -------------------------------------------------------------------------------------------------

    @Builder
    private Order(
            OrderId id,
            Long totalPrice,
            Long userId
    ) {
        this.id = id;
        this.totalPrice = totalPrice;
        this.status = OrderStatus.CREATED;
        this.userId = userId;
    }

    public void addProductOptions(List<OrderProductOption> productOptions) {
        List<OrderProductOption> those = new ArrayList<>(this.productOptions);
        those.addAll(productOptions);

        Set<Long> ids = new HashSet<>();

        for (OrderProductOption that : those) {
            Long id = that.getId();
            if (id != null && !ids.add(id)) {
                throw new BusinessException(CommonErrorType.CONFLICT);
            }

            OrderId orderId = that.getOrderId();
            if (orderId != null && !Objects.equals(this.id, orderId)) {
                throw new BusinessException(CommonErrorType.INCONSISTENT);
            }
        }

        this.productOptions = List.copyOf(those);
    }

}
