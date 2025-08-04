package com.loopers.domain.user;

import com.loopers.domain.user.attribute.Email;
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

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

class UserTest {

    @DisplayName("사용자를 생성할 때:")
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
            // given
            Gender gender = Gender.MALE;
            LocalDate birthDate = LocalDate.of(1990, 1, 1);
            Email email = new Email("gildong.go@example.com");

            // when & then
            assertThatException()
                    .isThrownBy(() -> User.builder()
                            .name(name)
                            .gender(gender)
                            .birthDate(birthDate)
                            .email(email)
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
        @ParameterizedTest
        void throwException_whenInvalidGenderCodeIsProvided(Gender gender) {
            // given
            String name = "gildong";
            LocalDate birthDate = LocalDate.of(1990, 1, 1);
            Email email = new Email("gildong.go@example.com");

            // when & then
            assertThatException()
                    .isThrownBy(() -> User.builder()
                            .name(name)
                            .gender(gender)
                            .birthDate(birthDate)
                            .email(email)
                            .build())
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorType", type(ErrorType.class))
                    .isEqualTo(CommonErrorType.INVALID);
        }

        @DisplayName("""
                생년월일이 주어지지 않으면,
                BusinessException(errorType=INVALID)이 발생한다.
                """)
        @NullSource
        @ParameterizedTest
        void throwException_whenBirthDateIsNotProvided(LocalDate birthDate) {
            // given
            String name = "gildong";
            Gender gender = Gender.MALE;
            Email email = new Email("gildong.go@example.com");

            // when & then
            assertThatException()
                    .isThrownBy(() -> User.builder()
                            .name(name)
                            .gender(gender)
                            .birthDate(birthDate)
                            .email(email)
                            .build())
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorType", type(ErrorType.class))
                    .isEqualTo(CommonErrorType.INVALID);
        }

        @DisplayName("""
                이메일이 주어지지 않으면,
                BusinessException(errorType=INVALID)이 발생한다.
                """)
        @NullSource
        @ParameterizedTest
        void throwException_whenEmailIsNotProvided(Email email) {
            // given
            String name = "gildong";
            Gender gender = Gender.MALE;
            LocalDate birthDate = LocalDate.of(1990, 1, 1);

            // when & then
            assertThatException()
                    .isThrownBy(() -> User.builder()
                            .name(name)
                            .gender(gender)
                            .birthDate(birthDate)
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
            Gender gender = Gender.MALE;
            LocalDate birthDate = LocalDate.of(1990, 1, 1);
            Email email = new Email("gildong.go@example.com");

            // when
            User user = User.builder()
                    .name(name)
                    .gender(gender)
                    .birthDate(birthDate)
                    .email(email)
                    .build();

            // then
            assertThat(user).isNotNull();
            assertThat(user.getName()).isEqualTo(name);
            assertThat(user.getGender()).isEqualTo(gender);
            assertThat(user.getBirthDate()).isEqualTo(birthDate);
            assertThat(user.getEmail()).isEqualTo(email);
        }

    }

}
