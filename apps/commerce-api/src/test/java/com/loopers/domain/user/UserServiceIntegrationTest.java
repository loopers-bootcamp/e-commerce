package com.loopers.domain.user;

import com.loopers.domain.user.attribute.Email;
import com.loopers.domain.user.attribute.Gender;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class UserServiceIntegrationTest {

    @InjectMocks
    private final UserService sut;

    @MockitoSpyBean
    private final UserRepository userRepository;

    private final TransactionTemplate transactionTemplate;
    private final TestEntityManager testEntityManager;
    private final DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("회원 정보를 조회할 때:")
    @Nested
    class GetUser {

        @DisplayName("이름과 일치하는 사용자가 없으면, Optional.empty를 반환한다.")
        @Test
        void throwException_whenUserAlreadyJoined() {
            // given
            String userName = "gildong";

            // when
            Optional<UserResult.GetUser> maybeResult = sut.getUser(userName);

            // then
            assertThat(maybeResult).isEmpty();

            verify(userRepository).findUserByName(userName);
        }

        @DisplayName("이름과 일치하는 사용자가 있으면 사용자 정보를 반환한다.")
        @Test
        void returnOptionalUser_whenUserExistsByName() {
            // given
            String userName = "gildong";

            User user = User.builder()
                    .name(userName)
                    .gender(Gender.MALE)
                    .birthDate(LocalDate.of(1990, 1, 1))
                    .email(new Email("gildong.go@example.com"))
                    .build();
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(user));

            // when
            Optional<UserResult.GetUser> maybeResult = sut.getUser(userName);

            // then
            assertThat(maybeResult).isPresent();
            assertThat(maybeResult.get().getUserId()).isEqualTo(user.getId());
            assertThat(maybeResult.get().getUserName()).isEqualTo(user.getName());
            assertThat(maybeResult.get().getGender()).isEqualTo(user.getGender());
            assertThat(maybeResult.get().getBirthDate()).isEqualTo(user.getBirthDate());
            assertThat(maybeResult.get().getEmail()).isEqualTo(user.getEmail());
        }

    }

    // -------------------------------------------------------------------------------------------------

    @DisplayName("회원 가입할 때:")
    @Nested
    class Join {

        @DisplayName("""
                이미 가입된 사용자면,
                BusinessException(errorType=CONFLICT)이 발생한다.
                """)
        @Test
        void throwException_whenUserAlreadyJoined() {
            // given
            User user = User.builder()
                    .name("gildong")
                    .gender(Gender.MALE)
                    .birthDate(LocalDate.of(1990, 1, 1))
                    .email(new Email("gildong.go@example.com"))
                    .build();
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(user));

            UserCommand.Join command = UserCommand.Join.builder()
                    .userName(user.getName())
                    .gender(Gender.FEMALE)
                    .birthDate(LocalDate.of(2010, 8, 15))
                    .email(new Email("gildong.hong@example.com"))
                    .build();

            // when & then
            assertThatException()
                    .isThrownBy(() -> sut.join(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorType", type(ErrorType.class))
                    .isEqualTo(CommonErrorType.CONFLICT);

            verify(userRepository).existsUserByName(any(String.class));
            verify(userRepository, never()).saveUser(any(User.class));
        }

        @DisplayName("모든 속성이 올바르면, 신규 회원을 등록하고 회원 정보를 반환한다.")
        @Test
        void returnUserInfo_whenAllPropertiesProvidedIsValid() {
            // given
            UserCommand.Join command = UserCommand.Join.builder()
                    .userName("gildong")
                    .gender(Gender.FEMALE)
                    .birthDate(LocalDate.of(2010, 8, 15))
                    .email(new Email("gildong.go@example.com"))
                    .build();

            // when
            UserResult.Join result = sut.join(command);

            // then
            assertThat(result.getUserName()).isEqualTo(command.getUserName());
            assertThat(result.getGender()).isEqualTo(command.getGender());
            assertThat(result.getBirthDate()).isEqualTo(command.getBirthDate());
            assertThat(result.getEmail()).isEqualTo(command.getEmail());

            verify(userRepository).existsUserByName(any(String.class));
            verify(userRepository).saveUser(any(User.class));
        }

    }

}
