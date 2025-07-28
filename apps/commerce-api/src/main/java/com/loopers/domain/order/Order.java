package com.loopers.domain.order;

import com.loopers.config.jpa.converter.OrderStatusConverter;
import com.loopers.domain.BaseEntity;
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
@Table(name = "orders", indexes = {
        @Index(columnList = "ref_user_id"),
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity implements Comparable<Order> {

    /**
     * 아이디
     */
    @Id
    @Column(name = "order_id", nullable = false, updatable = false)
    private UUID id;

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

    // -------------------------------------------------------------------------------------------------

    /**
     * 주문 상품 옵션 목록
     */
    @Transient
    private List<OrderProductOption> productOptions = Collections.emptyList();

    // -------------------------------------------------------------------------------------------------

    @Builder
    private Order(
            UUID id,
            Long totalPrice,
            Long userId
    ) {
        if (!OrderIdManager.isValid(id)) {
            throw new BusinessException(CommonErrorType.INVALID, "주문 아이디가 올바르지 않습니다.");
        }

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

            UUID orderId = that.getOrderId();
            if (orderId != null && !Objects.equals(this.id, orderId)) {
                throw new BusinessException(CommonErrorType.INCONSISTENT);
            }
        }

        this.productOptions = List.copyOf(those);
    }

    @Override
    public int compareTo(Order o) {
        long t1 = OrderIdManager.toTimestamp(this.id);
        long t2 = OrderIdManager.toTimestamp(o.id);

        int compared = Long.compare(t1, t2);
        if (compared != 0) {
            return compared;
        }

        return this.id.compareTo(o.id);
    }

}
