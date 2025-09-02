package com.loopers.domain;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.ZonedDateTime;
import java.util.UUID;

public record KafkaMessage<T>(
        String eventId,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS")
        ZonedDateTime publishedAt,
        T data
) {

    public static <T> KafkaMessage<T> from(T data) {
        return new KafkaMessage<>(UUID.randomUUID().toString(), ZonedDateTime.now(), data);
    }

    // -------------------------------------------------------------------------------------------------

}
