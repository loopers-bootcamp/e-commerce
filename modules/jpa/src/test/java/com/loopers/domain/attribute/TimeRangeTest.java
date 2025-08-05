package com.loopers.domain.attribute;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;

class TimeRangeTest {

    @DisplayName("생성할 때:")
    @Nested
    class Create {

        @DisplayName("시작일시가 종료일시보다 미래라면, IllegalArgumentException이 발생한다.")
        @CsvSource(textBlock = """
                2026-12-04T15:00:00Z | 2025-12-05T00:00:00Z
                2025-12-04T15:00:00Z | 2025-11-05T00:00:00Z
                2025-11-05T01:00:00Z | 2025-11-05T00:00:00Z
                2025-11-05T00:01:00Z | 2025-11-05T00:00:00Z
                2025-11-05T00:00:01Z | 2025-11-05T00:00:00Z
                """, delimiter = '|')
        @ParameterizedTest
        void throwException_whenStartIsAfterEnd(Instant startedAt, Instant endedAt) {
            // when & then
            assertThatException()
                    .isThrownBy(() -> TimeRange.of(startedAt, endedAt))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @DisplayName("유효한 값이면, TimeRange 객체를 생성한다.")
        @CsvSource(textBlock = """
                2025-11-05T00:00:00Z | 2025-11-05T00:00:00Z
                2025-11-05T00:00:00Z | 2025-11-05T00:00:01Z
                2025-11-05T00:00:00Z | 2025-11-05T00:01:00Z
                2025-11-05T00:00:00Z | 2025-11-05T01:00:00Z
                2025-11-05T00:00:00Z | 2025-11-06T00:00:00Z
                2025-11-05T00:00:00Z | 2025-12-05T00:00:00Z
                2025-11-05T00:00:00Z | 2026-11-05T00:00:00Z
                """, delimiter = '|')
        @ParameterizedTest
        void createNewTimeRangeWithValidInstants(Instant startedAt, Instant endedAt) {
            // when
            TimeRange range = TimeRange.of(startedAt, endedAt);

            // then
            assertThat(range).isNotNull();
            assertThat(range.getStartedAt()).isEqualTo(startedAt);
            assertThat(range.getEndedAt()).isEqualTo(endedAt);
        }

    }

    // -------------------------------------------------------------------------------------------------

    @DisplayName("기간 내에 포함되는지 반환할 때:")
    @Nested
    class Contains {

        @DisplayName("주어진 시간이 기간에 포함되면 true를, 아니면 false를 반환한다.")
        @CsvSource(textBlock = """
                2025-11-05T00:00:00Z | 2025-11-05T00:00:00Z | 2025-11-05T00:00:00Z || true
                2025-11-05T00:00:00Z | 2025-11-05T00:00:01Z | 2025-11-05T00:00:00Z || true
                2025-11-05T00:00:00Z | 2025-11-05T00:00:01Z | 2025-11-05T00:00:01Z || true
                2025-11-05T00:00:00Z | 2025-11-05T00:01:00Z | 2025-11-05T00:00:30Z || true
                2025-11-05T00:00:00Z | 2025-11-05T01:00:00Z | 2025-11-05T01:00:00Z || true
                2025-11-05T00:00:00Z | 2025-11-06T00:00:00Z | 2025-11-05T12:00:00Z || true
                2025-11-05T00:00:00Z | 2025-12-05T00:00:00Z | 2025-11-15T00:00:00Z || true
                2025-11-05T00:00:00Z | 2026-11-05T00:00:00Z | 2026-05-05T00:00:00Z || true
                2025-11-05T00:00:00Z | 2025-11-05T00:00:00Z | 2025-11-04T23:59:59Z || false
                2025-11-05T00:00:00Z | 2025-11-05T00:00:00Z | 2025-11-05T00:00:01Z || false
                2025-11-05T00:00:00Z | 2025-11-05T00:00:01Z | 2025-11-05T00:00:02Z || false
                2025-11-05T00:00:00Z | 2025-11-05T00:01:00Z | 2025-11-05T00:01:30Z || false
                2025-11-05T00:00:00Z | 2025-11-05T01:00:00Z | 2025-11-05T01:00:01Z || false
                2025-11-05T00:00:00Z | 2025-11-06T00:00:00Z | 2025-11-04T12:00:00Z || false
                2025-11-05T00:00:00Z | 2025-12-05T00:00:00Z | 2025-12-15T00:00:00Z || false
                2025-11-05T00:00:00Z | 2026-11-05T00:00:00Z | 2024-05-05T00:00:00Z || false
                """, delimiter = '|')
        @ParameterizedTest
        void checkIfTimeRangeContainsProvidedTime(Instant startedAt, Instant endedAt, Instant time, boolean expected) {
            // when
            TimeRange range = TimeRange.of(startedAt, endedAt);

            // then
            assertThat(range.contains(time)).isEqualTo(expected);
        }

    }

    // -------------------------------------------------------------------------------------------------

    @DisplayName("객체의 동등성을 비교할 때:")
    @Nested
    class EqualsAndHashCode {

        @DisplayName("같은 값을 가진 객체끼리 동등성을 보장한다.")
        @CsvSource(textBlock = """
                2025-11-05T00:00:00Z | 2025-11-05T00:00:00Z
                2025-11-05T00:00:00Z | 2025-11-05T00:00:01Z
                2025-11-05T00:00:00Z | 2025-11-05T00:01:00Z
                2025-11-05T00:00:00Z | 2025-11-05T01:00:00Z
                2025-11-05T00:00:00Z | 2025-11-06T00:00:00Z
                2025-11-05T00:00:00Z | 2025-12-05T00:00:00Z
                2025-11-05T00:00:00Z | 2026-11-05T00:00:00Z
                """, delimiter = '|')
        @ParameterizedTest
        void guaranteeEqualityForEachTimeRange(Instant startedAt, Instant endedAt) {
            // given
            TimeRange range = TimeRange.of(startedAt, endedAt);
            TimeRange other = TimeRange.of(startedAt, endedAt);

            // when & then
            assertThat(range).isEqualTo(other);
            assertThat(other).isEqualTo(range);
            assertThat(range.hashCode()).isEqualTo(other.hashCode());
            assertThat(other.hashCode()).isEqualTo(range.hashCode());
        }

    }

}
