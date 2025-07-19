package com.loopers.config.jpa.converter;

import com.loopers.domain.user.attribute.Email;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class EmailConverter implements AttributeConverter<Email, String> {

    @Override
    public String convertToDatabaseColumn(Email attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public Email convertToEntityAttribute(String dbData) {
        return Email.isValid(dbData) ? new Email(dbData) : null;
    }

}
