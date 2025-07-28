package com.loopers.domain.order;

import com.loopers.config.jpa.converter.OrderIdConverter;
import com.loopers.domain.BaseEntity;
import com.loopers.domain.order.attribute.OrderId;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "order_product_option", indexes = {
        @Index(name = "idx__order_product_option__ref_order_id", columnList = "orderId"),
        @Index(name = "idx__order_product_option__ref_product_option_id", columnList = "productOptionId"),
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderProductOption extends BaseEntity {

    /**
     * 아이디
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_product_option_id", nullable = false, updatable = false)
    private Long id;

    /**
     * 주문 시점의 가격
     */
    @Column(name = "price", nullable = false)
    private Long price;

    /**
     * 주문 수량
     */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    // -------------------------------------------------------------------------------------------------

    /**
     * 주문 아이디
     */
    @Convert(converter = OrderIdConverter.class)
    @Column(name = "ref_order_id", nullable = false, updatable = false)
    private OrderId orderId;

    /**
     * 상품 옵션 아이디
     */
    @Column(name = "ref_product_option_id", nullable = false, updatable = false)
    private Long productOptionId;

    // -------------------------------------------------------------------------------------------------

    @Builder
    private OrderProductOption(
            Long price,
            Integer quantity,
            OrderId orderId,
            Long productOptionId
    ) {
        this.price = price;
        this.quantity = quantity;
        this.orderId = orderId;
        this.productOptionId = productOptionId;
    }

}
