package com.loopers.domain.user;

import com.loopers.domain.user.attribute.Gender;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

class UserTest {

    @DisplayName("사용자를 생성할 때: ")
    @Nested
    class Create {

        @DisplayName("""
                이름이 영문 및 숫자로 10자 이내로 구성되지 않으면,
                BusinessException(errorType=INVALID)이 발생한다.
                """)
        @NullSource
        @EmptySource
        @ValueSource(strings = {
                " ",
                "GIL_DONG",
                "01234567890",
                "smith123456",
                "SmithBlackwood",
        })
        @ParameterizedTest
        void throwException_whenInvalidNameIsProvided(String name) {
            // when & then
            assertThatException()
                    .isThrownBy(() -> User.builder()
                            .name(name)
                            .genderCode(Gender.MALE.getCode())
                            .birthDate("1990-01-01")
                            .email("gildong.go@example.com")
                            .build())
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorType", type(ErrorType.class))
                    .isEqualTo(CommonErrorType.INVALID);
        }

        @DisplayName("""
                성별이 정의되지 않은 값이면,
                BusinessException(errorType=INVALID)이 발생한다.
                """)
        @NullSource
        @ValueSource(ints = {
                -1, 0, 100,
        })
        @ParameterizedTest
        void throwException_whenInvalidGenderCodeIsProvided(Integer genderCode) {
            // when & then
            assertThatException()
                    .isThrownBy(() -> User.builder()
                            .name("gildong")
                            .genderCode(genderCode)
                            .birthDate("1990-01-01")
                            .email("gildong.go@example.com")
                            .build())
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorType", type(ErrorType.class))
                    .isEqualTo(CommonErrorType.INVALID);
        }

        @DisplayName("""
                생년월일이 yyyy-MM-dd 형식에 맞지 않으면,
                BusinessException(errorType=INVALID)이 발생한다.
                """)
        @NullSource
        @EmptySource
        @ValueSource(strings = {
                " ",
                "1990-1-1",
                "1945-12-32",
                "2010.08.15",
                "07/12/2025",
        })
        @ParameterizedTest
        void throwException_whenInvalidBirthDateIsProvided(String birthDate) {
            // when & then
            assertThatException()
                    .isThrownBy(() -> User.builder()
                            .name("gildong")
                            .genderCode(Gender.MALE.getCode())
                            .birthDate(birthDate)
                            .email("gildong.go@example.com")
                            .build())
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorType", type(ErrorType.class))
                    .isEqualTo(CommonErrorType.INVALID);
        }

        @DisplayName("""
                이메일 형식이 올바르지 않으면,
                BusinessException(errorType=INVALID)이 발생한다.
                """)
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
        void throwException_whenInvalidEmailIsProvided(String email) {
            // when & then
            assertThatException()
                    .isThrownBy(() -> User.builder()
                            .name("gildong")
                            .genderCode(Gender.MALE.getCode())
                            .birthDate("1990-01-01")
                            .email(email)
                            .build())
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorType", type(ErrorType.class))
                    .isEqualTo(CommonErrorType.INVALID);
        }

        @DisplayName("모든 속성이 올바르면, 사용자를 생성한다.")
        @Test
        void createUser_whenAllPropertiesProvidedIsValid() {
            // given
            String name = "gildong";
            int genderCode = Gender.MALE.getCode();
            String birthDate = "1990-01-01";
            String email = "gildong.go@example.com";

            // when
            User user = User.builder()
                    .name(name)
                    .genderCode(genderCode)
                    .birthDate(birthDate)
                    .email(email)
                    .build();

            // then
            assertThat(user).isNotNull();
            assertThat(user.getName()).isEqualTo(name);
            assertThat(user.getGender().getCode()).isEqualTo(genderCode);
            assertThat(user.getBirthDate().toString()).isEqualTo(birthDate);
            assertThat(user.getEmail().getValue()).isEqualTo(email);
        }

    }

}
