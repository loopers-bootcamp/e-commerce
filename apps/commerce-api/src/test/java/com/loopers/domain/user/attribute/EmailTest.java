package com.loopers.domain.user.attribute;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

class EmailTest {

    @DisplayName("올바른 형식의 이메일이면 true를, 아니면 false를 반환한다.")
    @CsvSource(textBlock = """
                                                 | false
                    ''                           | false
                    ' '                          | false
                    foo.test.com                 | false
                    bar@example.c                | false
                    alpha/beta/gamma@test.org    | false
                    zeta001@whitehouse.alphabeta | false
                    omega002@bluehouse.org2      | false
                    alpha@mail.co                | true
                    beta123-admin@naver.com      | true
                    omega001&002@bluehouse.gamma | true
                    foo_bar+tag1-tag2@gmail.com  | true
                    1234@5678.org                | true
            """, delimiter = '|')
    @ParameterizedTest
    void checkIfEmailIsValid(String value, boolean expected) {
        // when
        boolean valid = Email.isValid(value);

        // then
        assertThat(valid).isEqualTo(expected);
    }

    @DisplayName("올바르지 않은 형식의 이메일이 주어지면, BusinessException(errorType=INVALID)이 발생한다.")
    @NullSource
    @EmptySource
    @ValueSource(strings = {
            " ",
            "foo.test.com",
            "bar@example.c",
            "alpha/beta/gamma@test.org",
            "zeta001@whitehouse.alphabeta",
            "omega002@bluehouse.org2",
    })
    @ParameterizedTest
    void throwException_whenInvalidEmailIsProvided(String value) {
        // when & then
        assertThatException()
                .isThrownBy(() -> new Email(value))
                .isInstanceOf(BusinessException.class)
                .extracting("errorType", type(ErrorType.class))
                .isEqualTo(CommonErrorType.INVALID);
    }

    @DisplayName("직렬화하면 단순 문자열이 된다.")
    @ValueSource(strings = {
            "alpha@mail.co",
            "beta123-admin@naver.com",
            "foo_bar+tag1-tag2@gmail.com",
            "1234@5678.org",
    })
    @ParameterizedTest
    void convertToString_whenJacksonSerialize(String value) throws JsonProcessingException {
        // given
        JsonMapper mapper = new JsonMapper();
        Email email = new Email(value);

        // when
        String json = mapper.writeValueAsString(email);

        // then
        assertThat(json).isEqualTo("\"%s\"".formatted(value));
    }

    @DisplayName("문자열을 사용하여 역직렬화된다.")
    @ValueSource(strings = {
            "alpha@mail.co",
            "beta123-admin@naver.com",
            "foo_bar+tag1-tag2@gmail.com",
            "1234@5678.org",
    })
    @ParameterizedTest
    void convertString_whenJacksonDeserialize(String value) throws JsonProcessingException {
        // given
        JsonMapper mapper = new JsonMapper();
        String json = "\"%s\"".formatted(value);

        // when
        Email email = mapper.readValue(json, Email.class);

        // then
        assertThat(email).isEqualTo(new Email(value));
    }

    @DisplayName("이메일에서 로컬 파트를 반환한다.")
    @CsvSource(textBlock = """
            alpha@mail.co               | alpha
            beta123-admin@naver.com     | beta123-admin
            foo_bar+tag1-tag2@gmail.com | foo_bar+tag1-tag2
            1234@5678.org               | 1234
            """, delimiter = '|')
    @ParameterizedTest
    void returnLocalPartFromEmail(String value, String expected) {
        // given
        Email email = new Email(value);

        // when
        String localPart = email.getLocalPart();

        // then
        assertThat(localPart).isEqualTo(expected);
    }

    @DisplayName("이메일에서 도메인을 반환한다.")
    @CsvSource(textBlock = """
            alpha@mail.co               | mail.co
            beta123-admin@naver.com     | naver.com
            foo_bar+tag1-tag2@gmail.com | gmail.com
            1234@5678.org               | 5678.org
            """, delimiter = '|')
    @ParameterizedTest
    void returnDomainFromEmail(String value, String expected) {
        // given
        Email email = new Email(value);

        // when
        String domain = email.getDomain();

        // then
        assertThat(domain).isEqualTo(expected);
    }

}
