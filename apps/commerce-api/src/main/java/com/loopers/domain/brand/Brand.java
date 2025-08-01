package com.loopers.domain.brand;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@Getter
@Entity
@Table(name = "brands")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Brand extends BaseEntity {

    /**
     * 아이디
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "brand_id", nullable = false, updatable = false)
    private Long id;

    /**
     * 브랜드 이름
     */
    @Column(name = "brand_name", nullable = false)
    private String name;

    /**
     * 브랜드 설명
     */
    @Column(name = "description")
    private String description;

    // -------------------------------------------------------------------------------------------------

    @Builder
    private Brand(String name, String description) {
        if (!StringUtils.hasText(name)) {
            throw new BusinessException(CommonErrorType.INVALID, "이름이 올바르지 않습니다.");
        }

        if (!StringUtils.hasText(description)) {
            throw new BusinessException(CommonErrorType.INVALID, "설명이 올바르지 않습니다.");
        }

        this.name = name;
        this.description = description;
    }

}
