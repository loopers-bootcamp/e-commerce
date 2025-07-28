package com.loopers.domain.order.attribute;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedEpochRandomGenerator;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;

@Getter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class OrderId implements Comparable<OrderId> {

    private static final TimeBasedEpochRandomGenerator generator = Generators.timeBasedEpochRandomGenerator();
    private static final Pattern UUID_PATTERN = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-8][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$"
    );

    @JsonValue
    @EqualsAndHashCode.Include
    private final UUID uuid;

    private final long timestamp;

    // -------------------------------------------------------------------------------------------------

    public OrderId() {
        UUID uuid = generator.generate();
        long msb = uuid.getMostSignificantBits();

        this.uuid = uuid;
        this.timestamp = (msb >>> 16) & 0xFFFFFFFFFFFFL;
    }

    @JsonCreator
    public OrderId(String value) {
        if (!isValid(value)) {
            throw new BusinessException(CommonErrorType.INVALID, "주문 아이디가 올바르지 않습니다.");
        }

        UUID uuid = UUID.fromString(value);
        long msb = uuid.getMostSignificantBits();

        this.uuid = uuid;
        this.timestamp = (msb >>> 16) & 0xFFFFFFFFFFFFL;
    }

    // -------------------------------------------------------------------------------------------------

    public static boolean isValid(String value) {
        if (!StringUtils.hasText(value) || !UUID_PATTERN.matcher(value).matches()) {
            return false;
        }

        UUID uuid = UUID.fromString(value);
        long msb = uuid.getMostSignificantBits();

        // UUIDv7: Unix epoch milliseconds
        long timestamp = (msb >>> 16) & 0xFFFFFFFFFFFFL;
        long now = System.currentTimeMillis();

        return now >= timestamp;
    }

    public Instant toInstant() {
        return Instant.ofEpochMilli(this.timestamp);
    }

    @Override
    public int compareTo(OrderId o) {
        return this.uuid.compareTo(o.uuid);
    }

}
