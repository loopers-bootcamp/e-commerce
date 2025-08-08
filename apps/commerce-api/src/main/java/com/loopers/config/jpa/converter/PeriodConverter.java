package com.loopers.config.jpa.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.Period;

@Converter
public class PeriodConverter implements AttributeConverter<Period, String> {

    @Override
    public String convertToDatabaseColumn(Period attribute) {
        return attribute == null ? null : attribute.toString();
    }

    @Override
    public Period convertToEntityAttribute(String dbData) {
        return dbData == null ? null : Period.parse(dbData);
    }

}
