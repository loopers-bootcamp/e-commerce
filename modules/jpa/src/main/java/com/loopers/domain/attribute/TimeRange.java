package com.loopers.domain.attribute;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.time.Instant;

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
    private Instant startedAt;

    /**
     * 종료일시
     */
    @EqualsAndHashCode.Include
    @Column(name = "ended_at", nullable = false)
    private Instant endedAt;

    public static TimeRange of(Instant startedAt, Instant endedAt) {
        if (startedAt == null) {
            throw new IllegalArgumentException("startedAt cannot be null");
        }

        if (endedAt == null) {
            throw new IllegalArgumentException("endedAt cannot be null");
        }

        if (startedAt.isAfter(endedAt)) {
            throw new IllegalArgumentException("startedAt cannot be after endedAt");
        }

        TimeRange range = new TimeRange();
        range.startedAt = startedAt;
        range.endedAt = endedAt;

        return range;
    }

    public boolean contains(Instant time) {
        if (time == null) {
            return false;
        }

        return !time.isBefore(this.startedAt) && !time.isAfter(this.endedAt);
    }

}
