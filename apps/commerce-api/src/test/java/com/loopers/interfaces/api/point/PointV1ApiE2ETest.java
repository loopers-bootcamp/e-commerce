package com.loopers.interfaces.api.point;

import com.loopers.annotation.SpringE2ETest;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.error.PointErrorType;
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
import org.junit.jupiter.params.provider.CsvSource;
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
class PointV1ApiE2ETest {

    private static final String BASE_ENDPOINT = "/api/v1/points";

    private final TestRestTemplate testRestTemplate;
    private final TransactionTemplate transactionTemplate;
    private final TestEntityManager testEntityManager;
    private final DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("GET " + BASE_ENDPOINT)
    @Nested
    class GetUser {

        private static final String REQUEST_URL = BASE_ENDPOINT;

        @DisplayName("""
                    X-USER-ID 헤더가 없다면,
                    400 응답과 { meta=BAD_REQUEST, data=null }를 보낸다.
                """)
        @Test
        void sendError_whenUserIdHeaderDoesNotExist() {
            // when
            HttpEntity<?> requestEntity = HttpEntity.EMPTY;

            ResponseEntity<ApiResponse<PointResponse.GetPoint>> response =
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

            ResponseEntity<ApiResponse<PointResponse.GetPoint>> response =
                    testRestTemplate.exchange(REQUEST_URL, HttpMethod.GET, requestEntity, new ParameterizedTypeReference<>() {
                    });

            // then
            assertThat(response.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND)).isTrue();
            assertThat(response.getBody().meta().errorCode()).isEqualTo(CommonErrorType.NOT_FOUND.getCode());
            assertThat(response.getBody().data()).isNull();
        }

        @DisplayName("존재하는 사용자 아이디를 주면, 보유 포인트 정보를 보낸다.")
        @Test
        void sendPointInfo_whenUserExistsByProvidedUserId() {
            // given
            String userName = "gildong";

            User user = User.builder()
                    .name(userName)
                    .genderCode(Gender.MALE.getCode())
                    .birthDate("1990-01-01")
                    .email("gildong.hong@example.com")
                    .build();
            Long userId = transactionTemplate.execute(status -> testEntityManager.persistAndGetId(user, Long.class));

            Point point = Point.builder()
                    .balance(70_000L)
                    .userId(userId)
                    .build();
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(point));

            // when
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-USER-ID", userName);
            HttpEntity<Object> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<ApiResponse<PointResponse.GetPoint>> response =
                    testRestTemplate.exchange(REQUEST_URL, HttpMethod.GET, requestEntity, new ParameterizedTypeReference<>() {
                    });

            // then
            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(response.getBody().data().getBalance()).isEqualTo(point.getBalance());
        }

    }

    // -------------------------------------------------------------------------------------------------

    @DisplayName("POST " + BASE_ENDPOINT + "/charge")
    @Nested
    class Charge {

        private static final String REQUEST_URL = BASE_ENDPOINT + "/charge";

        @DisplayName("""
                    존재하지 않는 사용자 아이디를 주면,
                    404 응답과 { meta=NOT_FOUND, data=null }를 보낸다.
                """)
        @Test
        void sendError_whenUserDoesNotExistByProvidedUserId() {
            // given
            String userName = "gildong";

            PointRequest.Charge body = PointRequest.Charge.builder()
                    .amount(1000L)
                    .build();

            // when
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-USER-ID", userName);
            HttpEntity<Object> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<ApiResponse<PointResponse.Charge>> response =
                    testRestTemplate.exchange(REQUEST_URL, HttpMethod.POST, requestEntity, new ParameterizedTypeReference<>() {
                    });

            // then
            assertThat(response.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND)).isTrue();
            assertThat(response.getBody().meta().errorCode()).isEqualTo(CommonErrorType.NOT_FOUND.getCode());
            assertThat(response.getBody().data()).isNull();
        }

        @DisplayName("""
                    금액을 지정하지 않거나 0 이하로 충전하려 하면,
                    400 응답과 { meta=INVALID, data=null }를 보낸다.
                """)
        @NullSource
        @ValueSource(longs = {
                Long.MIN_VALUE, -10000, -1000, -500, -100, -1, 0,
        })
        @ParameterizedTest
        void sendError_whenAmountIsNullOrZeroOrNegative(Long amount) {
            // given
            String userName = "gildong";

            User user = User.builder()
                    .name(userName)
                    .genderCode(Gender.MALE.getCode())
                    .birthDate("1990-01-01")
                    .email("gildong.hong@example.com")
                    .build();
            Long userId = transactionTemplate.execute(status -> testEntityManager.persistAndGetId(user, Long.class));

            Point point = Point.builder()
                    .balance(100_000L)
                    .userId(userId)
                    .build();
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(point));

            PointRequest.Charge body = PointRequest.Charge.builder()
                    .amount(amount)
                    .build();

            // when
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-USER-ID", userName);
            HttpEntity<Object> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<ApiResponse<PointResponse.Charge>> response =
                    testRestTemplate.exchange(REQUEST_URL, HttpMethod.POST, requestEntity, new ParameterizedTypeReference<>() {
                    });

            // then
            assertThat(response.getStatusCode().isSameCodeAs(HttpStatus.BAD_REQUEST)).isTrue();
            assertThat(response.getBody().meta().errorCode()).isEqualTo(CommonErrorType.INVALID.getCode());
            assertThat(response.getBody().data()).isNull();
        }

        @DisplayName("""
                    잔액이 최대치를 초과하면,
                    400 응답과 { meta=TOO_MUCH_BALANCE, data=null }를 보낸다.
                """)
        @CsvSource(textBlock = """
                0           | 100_000_001
                1           | 100_000_000
                99_990_000  | 20_000
                99_999_999  | 2
                100_000_000 | 1
                """, delimiter = '|')
        @ParameterizedTest
        void sendError_whenMaxBalanceExceeded(long balance, long amount) {
            // given
            String userName = "gildong";

            User user = User.builder()
                    .name(userName)
                    .genderCode(Gender.MALE.getCode())
                    .birthDate("1990-01-01")
                    .email("gildong.hong@example.com")
                    .build();
            Long userId = transactionTemplate.execute(status -> testEntityManager.persistAndGetId(user, Long.class));

            Point point = Point.builder()
                    .balance(balance)
                    .userId(userId)
                    .build();
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(point));

            PointRequest.Charge body = PointRequest.Charge.builder()
                    .amount(amount)
                    .build();

            // when
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-USER-ID", userName);
            HttpEntity<Object> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<ApiResponse<PointResponse.Charge>> response =
                    testRestTemplate.exchange(REQUEST_URL, HttpMethod.POST, requestEntity, new ParameterizedTypeReference<>() {
                    });

            // then
            assertThat(response.getStatusCode().isSameCodeAs(HttpStatus.BAD_REQUEST)).isTrue();
            assertThat(response.getBody().meta().errorCode()).isEqualTo(PointErrorType.TOO_MUCH_BALANCE.getCode());
            assertThat(response.getBody().data()).isNull();
        }

        @DisplayName("유효한 금액이라면, 충전된 잔액을 반환한다.")
        @CsvSource(textBlock = """
                0           | 100_000_000
                100_000     | 500
                1000        | 1000
                500         | 100_000
                99_999_999  | 1
                """, delimiter = '|')
        @ParameterizedTest
        void sendChargedBalance_whenValidAmountIsProvided(long balance, long amount) {
            // given
            String userName = "gildong";

            User user = User.builder()
                    .name(userName)
                    .genderCode(Gender.MALE.getCode())
                    .birthDate("1990-01-01")
                    .email("gildong.hong@example.com")
                    .build();
            Long userId = transactionTemplate.execute(status -> testEntityManager.persistAndGetId(user, Long.class));

            Point point = Point.builder()
                    .balance(balance)
                    .userId(userId)
                    .build();
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(point));

            PointRequest.Charge body = PointRequest.Charge.builder()
                    .amount(amount)
                    .build();

            // when
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-USER-ID", userName);
            HttpEntity<Object> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<ApiResponse<PointResponse.Charge>> response =
                    testRestTemplate.exchange(REQUEST_URL, HttpMethod.POST, requestEntity, new ParameterizedTypeReference<>() {
                    });

            // then
            long chargedBalance = balance + amount;
            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(response.getBody().data().getBalance()).isEqualTo(chargedBalance);
        }

    }

}
