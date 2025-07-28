package com.loopers.config.jpa.converter;

import com.loopers.domain.order.attribute.OrderId;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.UUID;

@Converter
public class OrderIdConverter implements AttributeConverter<OrderId, UUID> {

    @Override
    public UUID convertToDatabaseColumn(OrderId attribute) {
        return attribute == null ? null : attribute.getUuid();
    }

    @Override
    public OrderId convertToEntityAttribute(UUID dbData) {
        if (dbData == null) {
            return null;
        }

        String value = dbData.toString();
        return OrderId.isValid(value) ? new OrderId(value) : null;
    }

}
