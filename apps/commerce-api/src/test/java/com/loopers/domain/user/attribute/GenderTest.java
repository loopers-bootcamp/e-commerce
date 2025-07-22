package com.loopers.domain.user.attribute;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
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

    @DisplayName("직렬화하면 단순 숫자가 된다.")
    @EnumSource(Gender.class)
    @ParameterizedTest
    void convertToNumber_whenJacksonSerialize(Gender gender) throws JsonProcessingException {
        // given
        JsonMapper mapper = new JsonMapper();

        // when
        String json = mapper.writeValueAsString(gender);

        // then
        assertThat(json).isEqualTo("%d".formatted(gender.getCode()));
    }

    @DisplayName("숫자를 사용하여 역직렬화된다.")
    @EnumSource(Gender.class)
    @ParameterizedTest
    void convertNumber_whenJacksonDeserialize(Gender gender) throws JsonProcessingException {
        // given
        JsonMapper mapper = new JsonMapper();
        String json = "%d".formatted(gender.getCode());

        // when
        Gender convertedGender = mapper.readValue(json, Gender.class);

        // then
        assertThat(convertedGender).isEqualTo(gender);
    }

}
