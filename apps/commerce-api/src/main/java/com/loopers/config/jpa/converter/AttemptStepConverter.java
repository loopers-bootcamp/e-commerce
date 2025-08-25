package com.loopers.config.jpa.converter;

import com.loopers.domain.payment.attribute.AttemptStep;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class AttemptStepConverter implements AttributeConverter<AttemptStep, Integer> {

    @Override
    public Integer convertToDatabaseColumn(AttemptStep attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public AttemptStep convertToEntityAttribute(Integer dbData) {
        return AttemptStep.from(dbData);
    }

}
