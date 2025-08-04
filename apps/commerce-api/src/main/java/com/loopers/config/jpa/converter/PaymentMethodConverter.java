package com.loopers.config.jpa.converter;

import com.loopers.domain.payment.attribute.PaymentMethod;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class PaymentMethodConverter implements AttributeConverter<PaymentMethod, Integer> {

    @Override
    public Integer convertToDatabaseColumn(PaymentMethod attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public PaymentMethod convertToEntityAttribute(Integer dbData) {
        return PaymentMethod.from(dbData);
    }

}
