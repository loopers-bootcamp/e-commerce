package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "product_option")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductOption extends BaseEntity {

    /**
     * 아이디
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_option_id", nullable = false, updatable = false)
    private Long id;

    /**
     * 상품 옵션 이름
     */
    @Column(name = "product_option_name", nullable = false)
    private String name;

    /**
     * 추가 가격
     */
    @Column(name = "additional_price", nullable = false)
    private Integer additionalPrice;

    // -------------------------------------------------------------------------------------------------

    /**
     * 상품 아이디
     */
    @Column(name = "ref_product_id", nullable = false)
    private Long productId;

    // -------------------------------------------------------------------------------------------------

    @Builder
    private ProductOption(String name, Integer additionalPrice, Long productId) {
        this.name = name;
        this.additionalPrice = additionalPrice;
        this.productId = productId;
    }

}
