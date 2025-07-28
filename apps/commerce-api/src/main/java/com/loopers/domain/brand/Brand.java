package com.loopers.domain.brand;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "brands")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Brand extends BaseEntity {

    /**
     * 아이디
     */
    @Id
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
        this.name = name;
        this.description = description;
    }

}
