package com.loopers.config.jpa.converter;

import com.loopers.domain.order.attribute.OrderStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class OrderStatusConverter implements AttributeConverter<OrderStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(OrderStatus attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public OrderStatus convertToEntityAttribute(Integer dbData) {
        return OrderStatus.from(dbData);
    }

}
