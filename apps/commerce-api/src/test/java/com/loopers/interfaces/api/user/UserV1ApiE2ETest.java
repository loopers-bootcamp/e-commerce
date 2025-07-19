package com.loopers.interfaces.api.user;

import com.loopers.annotation.SpringE2ETest;
import com.loopers.domain.user.User;
import com.loopers.domain.user.attribute.Gender;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.error.CommonErrorType;
import com.loopers.utils.DatabaseCleanUp;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringE2ETest
@RequiredArgsConstructor
class UserV1ApiE2ETest {

    private static final String BASE_ENDPOINT = "/api/v1/users";

    private final TestRestTemplate testRestTemplate;
    private final TransactionTemplate transactionTemplate;
    private final TestEntityManager testEntityManager;
    private final DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("GET " + BASE_ENDPOINT + "/me")
    @Nested
    class GetUser {

        private static final String REQUEST_URL = BASE_ENDPOINT + "/me";

        @DisplayName("""
                    X-USER-ID 헤더가 없다면,
                    400 응답과 { meta=BAD_REQUEST, data=null }를 보낸다.
                """)
        @Test
        void sendError_whenUserIdHeaderDoesNotExist() {
            // when
            HttpEntity<?> requestEntity = HttpEntity.EMPTY;

            ResponseEntity<ApiResponse<UserResponse.GetUser>> response =
                    testRestTemplate.exchange(REQUEST_URL, HttpMethod.GET, requestEntity, new ParameterizedTypeReference<>() {
                    });

            // then
            assertThat(response.getStatusCode().isSameCodeAs(HttpStatus.BAD_REQUEST)).isTrue();
            assertThat(response.getBody().meta().errorCode()).isEqualTo(CommonErrorType.BAD_REQUEST.getCode());
            assertThat(response.getBody().data()).isNull();
        }

        @DisplayName("""
                    존재하지 않는 사용자 아이디를 주면,
                    404 응답과 { meta=NOT_FOUND, data=null }를 보낸다.
                """)
        @Test
        void sendError_whenUserDoesNotExistByProvidedUserId() {
            // given
            String userName = "gildong";

            // when
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-USER-ID", userName);
            HttpEntity<Object> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<ApiResponse<UserResponse.GetUser>> response =
                    testRestTemplate.exchange(REQUEST_URL, HttpMethod.GET, requestEntity, new ParameterizedTypeReference<>() {
                    });

            // then
            assertThat(response.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND)).isTrue();
            assertThat(response.getBody().meta().errorCode()).isEqualTo(CommonErrorType.NOT_FOUND.getCode());
            assertThat(response.getBody().data()).isNull();
        }

        @DisplayName("존재하는 사용자 아이디를 주면, 사용자 정보를 보낸다.")
        @Test
        void sendUserInfo_whenUserExistsByProvidedUserId() {
            // given
            String userName = "gildong";

            User user = User.builder()
                    .name(userName)
                    .genderCode(Gender.MALE.getCode())
                    .birthDate("1990-01-01")
                    .email("gildong.hong@example.com")
                    .build();
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(user));

            // when
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-USER-ID", userName);
            HttpEntity<Object> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<ApiResponse<UserResponse.GetUser>> response =
                    testRestTemplate.exchange(REQUEST_URL, HttpMethod.GET, requestEntity, new ParameterizedTypeReference<>() {
                    });

            // then
            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(response.getBody().data().getUserId()).isNotNull();
            assertThat(response.getBody().data().getUserName()).isEqualTo(userName);
            assertThat(response.getBody().data().getGenderCode()).isEqualTo(user.getGender().getCode());
            assertThat(response.getBody().data().getBirthDate()).isEqualTo(user.getBirthDate().toString());
            assertThat(response.getBody().data().getEmail()).isEqualTo(user.getEmail().getValue());
        }

    }

    // -------------------------------------------------------------------------------------------------

    @DisplayName("POST " + BASE_ENDPOINT)
    @Nested
    class Join {

        private static final String REQUEST_URL = BASE_ENDPOINT;

        @DisplayName("""
                    올바르지 않은 사용자 아이디를 주면,
                    400 응답과 { meta=INVALID, data=null }를 보낸다.
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
        void sendError_whenInvalidUserIdIsProvided(String userName) {
            // given
            UserRequest.Join body = UserRequest.Join.builder()
                    .userName(userName)
                    .genderCode(Gender.FEMALE.getCode())
                    .birthDate("2010-08-15")
                    .email("gildong.go@example.com")
                    .build();

            // when
            HttpEntity<Object> requestEntity = new HttpEntity<>(body);

            ResponseEntity<ApiResponse<UserResponse.Join>> response =
                    testRestTemplate.exchange(REQUEST_URL, HttpMethod.POST, requestEntity, new ParameterizedTypeReference<>() {
                    });

            // then
            assertThat(response.getStatusCode().isSameCodeAs(HttpStatus.BAD_REQUEST)).isTrue();
            assertThat(response.getBody().meta().errorCode()).isEqualTo(CommonErrorType.INVALID.getCode());
            assertThat(response.getBody().data()).isNull();
        }

        @DisplayName("""
                    올바르지 않은 성별을 주면,
                    400 응답과 { meta=INVALID, data=null }를 보낸다.
                """)
        @NullSource
        @ValueSource(ints = {
                -1, 0, 100,
        })
        @ParameterizedTest
        void sendError_whenInvalidGenderCodeIsProvided(Integer genderCode) {
            // given
            UserRequest.Join body = UserRequest.Join.builder()
                    .userName("gildong")
                    .genderCode(genderCode)
                    .birthDate("2010-08-15")
                    .email("gildong.go@example.com")
                    .build();

            // when
            HttpEntity<Object> requestEntity = new HttpEntity<>(body);

            ResponseEntity<ApiResponse<UserResponse.Join>> response =
                    testRestTemplate.exchange(REQUEST_URL, HttpMethod.POST, requestEntity, new ParameterizedTypeReference<>() {
                    });

            // then
            assertThat(response.getStatusCode().isSameCodeAs(HttpStatus.BAD_REQUEST)).isTrue();
            assertThat(response.getBody().meta().errorCode()).isEqualTo(CommonErrorType.INVALID.getCode());
            assertThat(response.getBody().data()).isNull();
        }

        @DisplayName("""
                    올바르지 않은 생년월일을 주면,
                    400 응답과 { meta=INVALID, data=null }를 보낸다.
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
        void sendError_whenInvalidBirthDateIsProvided(String birthDate) {
            // given
            UserRequest.Join body = UserRequest.Join.builder()
                    .userName("gildong")
                    .genderCode(Gender.MALE.getCode())
                    .birthDate(birthDate)
                    .email("gildong.go@example.com")
                    .build();

            // when
            HttpEntity<Object> requestEntity = new HttpEntity<>(body);

            ResponseEntity<ApiResponse<UserResponse.Join>> response =
                    testRestTemplate.exchange(REQUEST_URL, HttpMethod.POST, requestEntity, new ParameterizedTypeReference<>() {
                    });

            // then
            assertThat(response.getStatusCode().isSameCodeAs(HttpStatus.BAD_REQUEST)).isTrue();
            assertThat(response.getBody().meta().errorCode()).isEqualTo(CommonErrorType.INVALID.getCode());
            assertThat(response.getBody().data()).isNull();
        }

        @DisplayName("""
                    올바르지 않은 이메일을 주면,
                    400 응답과 { meta=INVALID, data=null }를 보낸다.
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
        void sendError_whenInvalidEmailIsProvided(String email) {
            // given
            UserRequest.Join body = UserRequest.Join.builder()
                    .userName("gildong")
                    .genderCode(Gender.MALE.getCode())
                    .birthDate("2010-08-15")
                    .email(email)
                    .build();

            // when
            HttpEntity<Object> requestEntity = new HttpEntity<>(body);

            ResponseEntity<ApiResponse<UserResponse.Join>> response =
                    testRestTemplate.exchange(REQUEST_URL, HttpMethod.POST, requestEntity, new ParameterizedTypeReference<>() {
                    });

            // then
            assertThat(response.getStatusCode().isSameCodeAs(HttpStatus.BAD_REQUEST)).isTrue();
            assertThat(response.getBody().meta().errorCode()).isEqualTo(CommonErrorType.INVALID.getCode());
            assertThat(response.getBody().data()).isNull();
        }

        @DisplayName("""
                    이미 가입된 사용자 아이디를 주면,
                    409 응답과 { meta=CONFLICT, data=null }를 보낸다.
                """)
        @Test
        void sendError_whenUserExistByProvidedUserId() {
            // given
            String userName = "gildong";

            User user = User.builder()
                    .name(userName)
                    .genderCode(Gender.MALE.getCode())
                    .birthDate("1990-01-01")
                    .email("gildong.hong@example.com")
                    .build();
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(user));

            UserRequest.Join body = UserRequest.Join.builder()
                    .userName(user.getName())
                    .genderCode(Gender.FEMALE.getCode())
                    .birthDate("2010-08-15")
                    .email("gildong.go@example.com")
                    .build();

            // when
            HttpEntity<Object> requestEntity = new HttpEntity<>(body);

            ResponseEntity<ApiResponse<UserResponse.Join>> response =
                    testRestTemplate.exchange(REQUEST_URL, HttpMethod.POST, requestEntity, new ParameterizedTypeReference<>() {
                    });

            // then
            assertThat(response.getStatusCode().isSameCodeAs(HttpStatus.CONFLICT)).isTrue();
            assertThat(response.getBody().meta().errorCode()).isEqualTo(CommonErrorType.CONFLICT.getCode());
            assertThat(response.getBody().data()).isNull();
        }

        @DisplayName("올바른 사용자 정보를 주면, 가입된 회원 정보를 보낸다.")
        @Test
        void sendUserInfo_whenProvidedUserInfoIsValid() {
            // given
            String userName = "gildong";

            UserRequest.Join body = UserRequest.Join.builder()
                    .userName(userName)
                    .genderCode(Gender.MALE.getCode())
                    .birthDate("1990-01-01")
                    .email("gildong.hong@example.com")
                    .build();

            // when
            HttpEntity<Object> requestEntity = new HttpEntity<>(body);

            ResponseEntity<ApiResponse<UserResponse.Join>> response =
                    testRestTemplate.exchange(REQUEST_URL, HttpMethod.POST, requestEntity, new ParameterizedTypeReference<>() {
                    });

            // then
            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(response.getBody().data().getUserId()).isNotNull();
            assertThat(response.getBody().data().getUserName()).isEqualTo(userName);
            assertThat(response.getBody().data().getGenderCode()).isEqualTo(body.getGenderCode());
            assertThat(response.getBody().data().getBirthDate()).isEqualTo(body.getBirthDate());
            assertThat(response.getBody().data().getEmail()).isEqualTo(body.getEmail());
        }

    }

}
