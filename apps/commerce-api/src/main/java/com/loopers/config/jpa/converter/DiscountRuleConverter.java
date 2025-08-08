package com.loopers.config.jpa.converter;

import com.loopers.domain.coupon.attribute.DiscountRule;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class DiscountRuleConverter implements AttributeConverter<DiscountRule, Integer> {

    @Override
    public Integer convertToDatabaseColumn(DiscountRule attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public DiscountRule convertToEntityAttribute(Integer dbData) {
        return DiscountRule.from(dbData);
    }

}
