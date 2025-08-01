package com.loopers.interfaces.api.activity;

import com.loopers.annotation.SpringE2ETest;
import com.loopers.domain.product.Product;
import com.loopers.domain.user.User;
import com.loopers.domain.user.attribute.Email;
import com.loopers.domain.user.attribute.Gender;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.utils.DatabaseCleanUp;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringE2ETest
@RequiredArgsConstructor
class LikeV1ApiE2ETest {

    private static final String BASE_ENDPOINT = "/api/v1/like";

    private final TestRestTemplate testRestTemplate;
    private final TransactionTemplate transactionTemplate;
    private final TestEntityManager testEntityManager;
    private final DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("POST " + BASE_ENDPOINT + "/products/{productId}")
    @Nested
    class Like {

        private static final String REQUEST_URL = BASE_ENDPOINT + "/products/{productId}";

        @DisplayName("사용자가 같은 상품에 좋아요를 몇 번을 표시해도, 멱등성을 보장한다.")
        @Test
        void shouldBeIdempotent_whenUserLikesTheSameProductMultipleTimes() {
            // given
            String userName = "gildong";
            int requestCount = 5;

            User user = User.builder()
                    .name(userName)
                    .gender(Gender.MALE)
                    .birthDate(LocalDate.of(1990, 1, 1))
                    .email(new Email("gildong.hong@example.com"))
                    .build();
            Product product = Product.builder()
                    .name("FooBarQux")
                    .basePrice(1000)
                    .brandId(null)
                    .build();
            transactionTemplate.executeWithoutResult(status ->
                    Stream.of(user, product).forEach(testEntityManager::persist));

            // when
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-USER-ID", userName);
            HttpEntity<Object> requestEntity = new HttpEntity<>(headers);

            String url = UriComponentsBuilder.fromPath(REQUEST_URL)
                    .buildAndExpand(product.getId())
                    .toUriString();

            // then
            for (int i = 0; i < requestCount; i++) {
                ResponseEntity<ApiResponse<Boolean>> response =
                        testRestTemplate.exchange(url, HttpMethod.POST, requestEntity, new ParameterizedTypeReference<>() {
                        });

                assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
                assertThat(response.getBody().data()).isTrue();
            }
        }

    }

    // -------------------------------------------------------------------------------------------------

    @DisplayName("DELETE " + BASE_ENDPOINT + "/products/{productId}")
    @Nested
    class Dislike {

        private static final String REQUEST_URL = BASE_ENDPOINT + "/products/{productId}";

        @DisplayName("사용자가 같은 상품에 좋아요를 몇 번을 취소해도, 멱등성을 보장한다.")
        @Test
        void shouldBeIdempotent_whenUserDislikesTheSameProductMultipleTimes() {
            // given
            String userName = "gildong";
            int requestCount = 5;

            User user = User.builder()
                    .name(userName)
                    .gender(Gender.MALE)
                    .birthDate(LocalDate.of(1990, 1, 1))
                    .email(new Email("gildong.hong@example.com"))
                    .build();
            Product product = Product.builder()
                    .name("FooBarQux")
                    .basePrice(1000)
                    .brandId(null)
                    .build();
            transactionTemplate.executeWithoutResult(status ->
                    Stream.of(user, product).forEach(testEntityManager::persist));

            // when
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-USER-ID", userName);
            HttpEntity<Object> requestEntity = new HttpEntity<>(headers);

            String url = UriComponentsBuilder.fromPath(REQUEST_URL)
                    .buildAndExpand(product.getId())
                    .toUriString();

            // then
            for (int i = 0; i < requestCount; i++) {
                ResponseEntity<ApiResponse<Boolean>> response =
                        testRestTemplate.exchange(url, HttpMethod.DELETE, requestEntity, new ParameterizedTypeReference<>() {
                        });

                assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
                assertThat(response.getBody().data()).isTrue();
            }
        }

    }

}
