package com.loopers.config.jpa.converter;

import com.loopers.domain.payment.attribute.CardNumber;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class CardNumberConverter implements AttributeConverter<CardNumber, String> {

    @Override
    public String convertToDatabaseColumn(CardNumber attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public CardNumber convertToEntityAttribute(String dbData) {
        return CardNumber.isValid(dbData) ? new CardNumber(dbData) : null;
    }

}
