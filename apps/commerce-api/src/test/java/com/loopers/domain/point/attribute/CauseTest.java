package com.loopers.domain.point.attribute;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class CauseTest {

    @DisplayName("알맞은 코드가 주어지면, 원인으로 변환한다.")
    @EnumSource(Cause.class)
    @ParameterizedTest
    void convertToCause_whenValidCodeIsProvided(Cause expected) {
        // given
        int code = expected.getCode();

        // when
        Cause cause = Cause.from(code);

        // then
        assertThat(cause).returns(expected.name(), Cause::name);
    }

    @DisplayName("잘못된 코드가 주어지면, null을 반환한다.")
    @Test
    void returnNull_whenInvalidCodeIsProvided() {
        // given
        int code = -1;

        // when
        Cause cause = Cause.from(code);

        // then
        assertThat(cause).isNull();
    }

}
