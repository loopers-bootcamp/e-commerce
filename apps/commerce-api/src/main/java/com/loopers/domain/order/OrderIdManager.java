package com.loopers.domain.order;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedEpochRandomGenerator;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrderIdManager {

    private static final TimeBasedEpochRandomGenerator generator = Generators.timeBasedEpochRandomGenerator();

    public static UUID generate() {
        return generator.generate();
    }

    public static boolean isValid(UUID orderId) {
        if (orderId == null) {
            return false;
        }

        long timestamp = toTimestamp(orderId);
        long now = System.currentTimeMillis();

        return now >= timestamp;
    }

    public static long toTimestamp(UUID orderId) {
        // UUIDv7: Unix epoch milliseconds
        long msb = orderId.getMostSignificantBits();
        return (msb >>> 16) & 0xFFFFFFFFFFFFL;
    }

}
