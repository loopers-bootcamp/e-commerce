package com.loopers.config.jpa.converter;

import com.loopers.domain.user.attribute.Gender;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class GenderConverter implements AttributeConverter<Gender, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Gender attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public Gender convertToEntityAttribute(Integer dbData) {
        return Gender.from(dbData);
    }

}
