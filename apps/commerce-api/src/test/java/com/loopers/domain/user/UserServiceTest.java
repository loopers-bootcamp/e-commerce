package com.loopers.domain.user;

import com.loopers.domain.user.attribute.Email;
import com.loopers.domain.user.attribute.Gender;
import com.loopers.support.error.BusinessException;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@MockitoSettings
class UserServiceTest {

    @InjectMocks
    private UserService sut;

    @Mock
    private UserRepository userRepository;

    @DisplayName("사용자를 조회할 때:")
    @Nested
    class GetUser {

        @DisplayName("이름과 일치하는 사용자가 없으면, Optional.empty를 반환한다.")
        @Test
        void returnEmptyOptional_whenUserDoesNotExistByName() {
            // given
            String name = "gildong";
            given(userRepository.findUserByName(name))
                    .willReturn(Optional.empty());

            // when
            Optional<UserResult.GetUser> maybeResult = sut.getUser(name);

            // then
            assertThat(maybeResult).isEmpty();
        }

        @DisplayName("이름과 일치하는 사용자가 있으면 사용자 정보를 반환한다.")
        @Test
        void returnOptionalUser_whenUserExistsByName() {
            // given
            String name = "gildong";

            User foundUser = Instancio.of(User.class)
                    .set(field(User::getName), name)
                    .create();
            given(userRepository.findUserByName(name))
                    .willReturn(Optional.of(foundUser));

            // when
            Optional<UserResult.GetUser> maybeResult = sut.getUser(name);

            // then
            assertThat(maybeResult).isPresent();
            assertThat(maybeResult.get().getUserId()).isEqualTo(foundUser.getId());
            assertThat(maybeResult.get().getUserName()).isEqualTo(foundUser.getName());
            assertThat(maybeResult.get().getGender()).isEqualTo(foundUser.getGender());
            assertThat(maybeResult.get().getBirthDate()).isEqualTo(foundUser.getBirthDate());
            assertThat(maybeResult.get().getEmail()).isEqualTo(foundUser.getEmail());
        }

    }

    // -------------------------------------------------------------------------------------------------

    @DisplayName("회원 가입할 때:")
    @Nested
    class Join {

        @DisplayName("이름, 성별, 생년월일, 이메일이 모두 올바르면 사용자 정보를 반환한다.")
        @Test
        void throwException_whenUserDoesNotExistByName() {
            // given
            UserCommand.Join command = UserCommand.Join.builder()
                    .userName("gildong")
                    .gender(Gender.MALE)
                    .birthDate(LocalDate.of(1990, 1, 1))
                    .email(new Email("gildong.hong@example.com"))
                    .build();
            given(userRepository.existsUserByName(command.getUserName()))
                    .willReturn(true);

            // when & then
            assertThatException()
                    .isThrownBy(() -> sut.join(command))
                    .isInstanceOf(BusinessException.class);
        }

        @DisplayName("이미 가입된 회원의 이름이 아니고 모든 속성이 올바르면, 사용자 정보를 반환한다.")
        @Test
        void returnUser_whenUserExistsByName() {
            // given
            UserCommand.Join command = UserCommand.Join.builder()
                    .userName("gildong")
                    .gender(Gender.MALE)
                    .birthDate(LocalDate.of(1990, 1, 1))
                    .email(new Email("gildong.hong@example.com"))
                    .build();
            given(userRepository.existsUserByName(command.getUserName()))
                    .willReturn(false);

            // when
            UserResult.Join result = sut.join(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getUserName()).isEqualTo(command.getUserName());
            assertThat(result.getGender()).isEqualTo(command.getGender());
            assertThat(result.getBirthDate()).isEqualTo(command.getBirthDate());
            assertThat(result.getEmail()).isEqualTo(command.getEmail());

            verify(userRepository).existsUserByName(command.getUserName());
            verify(userRepository).saveUser(any(User.class));
        }

    }

}
