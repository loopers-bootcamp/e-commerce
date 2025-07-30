package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

@Getter
@Entity
@Table(name = "products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    /**
     * 아이디
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
        if (!StringUtils.hasText(name)) {
            throw new BusinessException(CommonErrorType.INVALID, "이름이 올바르지 않습니다.");
        }

        if (basePrice == null || basePrice < 0) {
            throw new BusinessException(CommonErrorType.INVALID, "기본 가격은 0 이상이어야 합니다.");
        }

        this.name = name;
        this.basePrice = basePrice;
        this.brandId = brandId;
    }

    public void addOptions(List<ProductOption> options) {
        if (CollectionUtils.isEmpty(options)) {
            return;
        }

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
