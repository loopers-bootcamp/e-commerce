package com.loopers.domain.order;

import com.loopers.domain.BaseEntity;
import com.loopers.support.AddedItem;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 주문 도메인에서의 상품은 SKU(Stock Keeping Unit)다.
 * <p>
 * 상품 도메인에서 SKU가 {@code Product}든 {@code ProductOption}이든,
 * 주문 컨텍스트에서는 '상품'이라는 것만 인지하면 된다.
 * 이 엔터티는 {@code ProductOption}와 관계되었기에 {@code OrderProductOption}이라고
 * 명명하는 게 직관적일 순 있어도, 물리적으로 분리된 MSA를 생각하면 그 장점 또한 퇴색된다고 생각한다.
 * <p>
 * 따라서 간단하게 {@code OrderProduct}라고 이름을 정의한다.
 */
@Getter
@Entity
@Table(name = "order_products", indexes = {
        @Index(columnList = "ref_order_id"),
        @Index(columnList = "ref_product_option_id"),
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderProduct extends BaseEntity implements AddedItem<Long, UUID> {

    /**
     * 아이디
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_product_id", nullable = false, updatable = false)
    private Long id;

    /**
     * 주문 시점의 가격
     */
    @Column(name = "price", nullable = false)
    private Integer price;

    /**
     * 주문 수량
     */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    // -------------------------------------------------------------------------------------------------

    /**
     * 주문 아이디
     */
    @Column(name = "ref_order_id", nullable = false, updatable = false)
    private UUID orderId;

    /**
     * 상품 옵션 아이디
     */
    @Column(name = "ref_product_option_id", nullable = false, updatable = false)
    private Long productOptionId;

    // -------------------------------------------------------------------------------------------------

    @Builder
    private OrderProduct(
            Integer price,
            Integer quantity,
            UUID orderId,
            Long productOptionId
    ) {
        if (price == null || price < 0) {
            throw new BusinessException(CommonErrorType.INVALID, "주문 시점의 가격은 0 이상이어야 합니다.");
        }

        if (quantity == null || quantity <= 0) {
            throw new BusinessException(CommonErrorType.INVALID, "주문 수량은 1 이상이어야 합니다.");
        }

        if (orderId == null) {
            throw new BusinessException(CommonErrorType.INVALID, "주문 아이디가 올바르지 않습니다.");
        }

        if (productOptionId == null) {
            throw new BusinessException(CommonErrorType.INVALID, "상품 옵션 아이디가 올바르지 않습니다.");
        }

        this.price = price;
        this.quantity = quantity;
        this.orderId = orderId;
        this.productOptionId = productOptionId;
    }

    @Override
    public UUID getParentId() {
        return this.orderId;
    }

}
