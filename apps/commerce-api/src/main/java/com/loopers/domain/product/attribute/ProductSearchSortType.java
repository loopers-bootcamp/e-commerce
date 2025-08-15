package com.loopers.domain.product.attribute;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 상품 목록 조회 정렬 기준
 */
@Getter
@RequiredArgsConstructor
public enum ProductSearchSortType {

    LATEST("최신순"),
    POPULAR("인기순"),
    CHEAP("낮은가격순");

    private final String description;

}
