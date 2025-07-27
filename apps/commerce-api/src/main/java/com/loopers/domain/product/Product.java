package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
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
@Table(name = "product")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    /**
     * 아이디
     */
    @Id
    @Column(name = "product_id", nullable = false, updatable = false)
    private Long id;

    /**
     * 상품 이름
     */
    @Column(name = "product_name", nullable = false)
    private String name;

    /**
     * 기본 가격
     */
    @Column(name = "base_price", nullable = false)
    private Integer basePrice;

    // -------------------------------------------------------------------------------------------------

    /**
     * 브랜드 아이디
     */
    @Column(name = "ref_brand_id")
    private Long brandId;

    // -------------------------------------------------------------------------------------------------

    /**
     * 옵션 목록
     */
    @Transient
    private List<ProductOption> options = Collections.emptyList();

    // -------------------------------------------------------------------------------------------------

    @Builder
    private Product(String name, Integer basePrice, Long brandId) {
        this.name = name;
        this.basePrice = basePrice;
        this.brandId = brandId;
    }

    public void addOption(ProductOption option) {
        addOptions(List.of(option));
    }

    public void addOptions(List<ProductOption> options) {
        List<ProductOption> those = new ArrayList<>(this.options);
        those.addAll(options);

        Set<Long> ids = new HashSet<>();

        for (ProductOption that : those) {
            Long id = that.getId();
            if (id != null && !ids.add(id)) {
                throw new BusinessException(CommonErrorType.CONFLICT);
            }

            Long productId = that.getProductId();
            if (productId != null && !Objects.equals(this.id, productId)) {
                throw new BusinessException(CommonErrorType.INCONSISTENT);
            }
        }

        this.options = List.copyOf(those);
    }

    public int getActualPrice(Long optionId) {
        ProductOption opt = this.options.stream()
                .filter(Objects::nonNull)
                .filter(option -> Objects.equals(option.getId(), optionId))
                .findFirst()
                .orElseThrow();

        return this.basePrice + opt.getAdditionalPrice();
    }

}
