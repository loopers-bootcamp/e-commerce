package com.loopers.domain.attribute;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.time.*;
import java.time.temporal.TemporalAmount;

/**
 * @see <a href="https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/codedeploy/model/TimeRange.html">
 * com.amazonaws.services.codedeploy.model.TimeRange</a>
 */
@Getter
@ToString
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public final class TimeRange {

    /**
     * 시작일시
     */
    @EqualsAndHashCode.Include
    @Column(name = "started_at", nullable = false)
    private ZonedDateTime startedAt;

    /**
     * 종료일시
     */
    @EqualsAndHashCode.Include
    @Column(name = "ended_at", nullable = false)
    private ZonedDateTime endedAt;

    // -------------------------------------------------------------------------------------------------

    public static final TimeRange WHENEVER = new TimeRange(
            ZonedDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC),
            ZonedDateTime.of(LocalDateTime.MAX.withYear(9999).minusSeconds(1), ZoneOffset.UTC)
    );

    public static TimeRange of(ZonedDateTime startedAt, ZonedDateTime endedAt) {
        return new TimeRange(startedAt, endedAt);
    }

    public static TimeRange of(TemporalAmount amountToAdd) {
        ZonedDateTime startedAt = ZonedDateTime.now();
        ZonedDateTime endedAt = startedAt.plus(amountToAdd);
        return new TimeRange(startedAt, endedAt);
    }

    private TimeRange(ZonedDateTime startedAt, ZonedDateTime endedAt) {
        if (startedAt == null) {
            throw new IllegalArgumentException("startedAt cannot be null");
        }

        if (endedAt == null) {
            throw new IllegalArgumentException("endedAt cannot be null");
        }

        if (startedAt.isAfter(endedAt)) {
            throw new IllegalArgumentException("startedAt cannot be after endedAt");
        }

        this.startedAt = startedAt;
        this.endedAt = endedAt;
    }

    public boolean contains(Instant time) {
        if (time == null) {
            return false;
        }

        return !time.isBefore(this.startedAt.toInstant()) && !time.isAfter(this.endedAt.toInstant());
    }

}
