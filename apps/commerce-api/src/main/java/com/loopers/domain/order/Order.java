package com.loopers.domain.order;

import com.loopers.config.jpa.converter.OrderStatusConverter;
import com.loopers.domain.BaseEntity;
import com.loopers.domain.order.attribute.OrderStatus;
import com.loopers.domain.order.error.OrderErrorType;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.util.CollectionUtils;

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
     * 주문 상품 목록
     */
    @Transient
    private List<OrderProduct> products = Collections.emptyList();

    // -------------------------------------------------------------------------------------------------

    @Builder
    private Order(
            UUID id,
            Long totalPrice,
            Long userId
    ) {
        if (id == null) {
            throw new BusinessException(CommonErrorType.INVALID, "주문 아이디가 올바르지 않습니다.");
        }

        long msb = id.getMostSignificantBits();
        long timestamp = (msb >>> 16) & 0xFFFFFFFFFFFFL;
        long now = System.currentTimeMillis();

        if (now < timestamp) {
            throw new BusinessException(CommonErrorType.INVALID, "주문 아이디가 올바르지 않습니다.");
        }

        if (totalPrice == null || totalPrice < 0) {
            throw new BusinessException(CommonErrorType.INVALID, "총 가격은 0 이상이어야 합니다.");
        }

        if (userId == null) {
            throw new BusinessException(CommonErrorType.INVALID, "사용자 아이디가 올바르지 않습니다.");
        }

        this.id = id;
        this.totalPrice = totalPrice;
        this.status = OrderStatus.CREATED;
        this.userId = userId;
    }

    public void addProducts(List<OrderProduct> products) {
        if (CollectionUtils.isEmpty(products)) {
            return;
        }

        List<OrderProduct> those = new ArrayList<>(this.products);
        those.addAll(products);

        Set<Long> ids = new HashSet<>();

        for (OrderProduct that : those) {
            Long id = that.getId();
            if (id != null && !ids.add(id)) {
                throw new BusinessException(CommonErrorType.CONFLICT);
            }

            UUID orderId = that.getOrderId();
            if (!Objects.equals(this.id, orderId)) {
                throw new BusinessException(CommonErrorType.INCONSISTENT);
            }
        }

        this.products = List.copyOf(those);
    }

    public void complete() {
        if (this.status.isConcluding()) {
            throw new BusinessException(OrderErrorType.CONCLUDING);
        }

        this.status = OrderStatus.COMPLETE;
    }

    public void expire() {
        if (this.status.isConcluding()) {
            throw new BusinessException(OrderErrorType.CONCLUDING);
        }

        this.status = OrderStatus.EXPIRED;
    }

    public void cancel() {
        if (this.status.isConcluding()) {
            throw new BusinessException(OrderErrorType.CONCLUDING);
        }

        this.status = OrderStatus.CANCELED;
    }

    public long toTimestamp() {
        // UUIDv7: Unix epoch milliseconds
        long msb = this.id.getMostSignificantBits();
        return (msb >>> 16) & 0xFFFFFFFFFFFFL;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Order order = (Order) o;
        return getId() != null && Objects.equals(getId(), order.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }

    @Override
    public int compareTo(Order o) {
        long t1 = this.toTimestamp();
        long t2 = o.toTimestamp();

        int compared = Long.compare(t1, t2);
        if (compared != 0) {
            return compared;
        }

        return this.id.compareTo(o.id);
    }

}
