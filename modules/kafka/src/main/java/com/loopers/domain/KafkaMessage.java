package com.loopers.domain;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.ZonedDateTime;
import java.util.UUID;

public record KafkaMessage<T>(
        String eventId,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "Asia/Seoul")
        ZonedDateTime publishedAt,
        T payload
) {

    public static <T> KafkaMessage<T> from(T payload) {
        return new KafkaMessage<>(UUID.randomUUID().toString(), ZonedDateTime.now(), payload);
    }

    // -------------------------------------------------------------------------------------------------

}
