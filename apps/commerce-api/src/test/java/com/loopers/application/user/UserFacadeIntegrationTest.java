package com.loopers.application.user;

import com.loopers.domain.point.PointService;
import com.loopers.domain.user.UserCommand;
import com.loopers.domain.user.UserService;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class UserFacadeIntegrationTest {

    @InjectMocks
    private final UserFacade sut;

    @MockitoSpyBean
    private final UserService userService;
    @MockitoSpyBean
    private final PointService pointService;

    private final DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("회원 가입하고 회원 정보를 조회할 때: ")
    @Nested
    class JoinAndGetUser {

        @DisplayName("""
                이미 가입된 사용자면,
                BusinessException(errorType=CONFLICT)이 발생한다.
                """)
        @Test
        void throwException_whenUserAlreadyJoined() {
            // given
            UserInput.Join input1 = UserInput.Join.builder()
                    .userName("gildong")
                    .gender(Gender.FEMALE)
                    .birthDate(LocalDate.of(1990, 1, 1))
                    .email(new Email("gildong.hong@example.com"))
                    .build();

            UserInput.Join input2 = UserInput.Join.builder()
                    .userName("gildong")
                    .gender(Gender.MALE)
                    .birthDate(LocalDate.of(2010, 8, 15))
                    .email(new Email("gildong.go@example.com"))
                    .build();

            // when & then
            sut.join(input1);
            sut.getUser(input1.getUserName());

            assertThatException()
                    .isThrownBy(() -> {
                        sut.join(input2);
                        sut.getUser(input2.getUserName());
                    })
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorType", type(ErrorType.class))
                    .isEqualTo(CommonErrorType.CONFLICT);

            verify(userService, times(2)).join(any(UserCommand.Join.class));
            verify(userService, times(1)).getUser(any(String.class));
            verify(pointService, times(1)).create(any(Long.class));
        }

        @DisplayName("모든 속성이 올바르면, 신규 회원을 등록하고 회원 정보를 반환한다.")
        @Test
        void returnUserInfo_whenAllPropertiesProvidedIsValid() {
            // given
            UserInput.Join input = UserInput.Join.builder()
                    .userName("gildong")
                    .gender(Gender.FEMALE)
                    .birthDate(LocalDate.of(2010, 8, 15))
                    .email(new Email("gildong.go@example.com"))
                    .build();

            // when
            sut.join(input);
            UserOutput.GetUser output = sut.getUser(input.getUserName());

            // then
            assertThat(output.getUserName()).isEqualTo(input.getUserName());
            assertThat(output.getGender()).isEqualTo(input.getGender());
            assertThat(output.getBirthDate()).isEqualTo(input.getBirthDate());
            assertThat(output.getEmail()).isEqualTo(input.getEmail());

            verify(userService).join(any(UserCommand.Join.class));
            verify(userService).getUser(any(String.class));
            verify(pointService).create(any(Long.class));
        }

    }

}
