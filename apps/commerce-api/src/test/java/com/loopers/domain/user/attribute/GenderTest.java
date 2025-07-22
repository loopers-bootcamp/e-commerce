package com.loopers.domain.user.attribute;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class GenderTest {

    @DisplayName("알맞은 코드가 주어지면, 성별로 변환한다.")
    @EnumSource(Gender.class)
    @ParameterizedTest
    void convertToGender_whenValidCodeIsProvided(Gender expected) {
        // given
        int code = expected.getCode();

        // when
        Gender gender = Gender.from(code);

        // then
        assertThat(gender).returns(expected.name(), Gender::name);
    }

    @DisplayName("잘못된 코드가 주어지면, null을 반환한다.")
    @Test
    void returnNull_whenInvalidCodeIsProvided() {
        // given
        int code = -1;

        // when
        Gender gender = Gender.from(code);

        // then
        assertThat(gender).isNull();
    }

}
