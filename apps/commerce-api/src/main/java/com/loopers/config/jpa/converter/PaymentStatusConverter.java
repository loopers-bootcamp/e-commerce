package com.loopers.config.jpa.converter;

import com.loopers.domain.payment.attribute.PaymentStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class PaymentStatusConverter implements AttributeConverter<PaymentStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(PaymentStatus attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public PaymentStatus convertToEntityAttribute(Integer dbData) {
        return PaymentStatus.from(dbData);
    }

}
