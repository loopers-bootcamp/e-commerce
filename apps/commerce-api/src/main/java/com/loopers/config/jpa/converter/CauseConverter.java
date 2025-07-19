package com.loopers.config.jpa.converter;

import com.loopers.domain.point.attribute.Cause;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class CauseConverter implements AttributeConverter<Cause, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Cause attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public Cause convertToEntityAttribute(Integer dbData) {
        return Cause.from(dbData);
    }

}
