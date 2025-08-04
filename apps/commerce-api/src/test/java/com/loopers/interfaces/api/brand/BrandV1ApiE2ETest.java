package com.loopers.interfaces.api.brand;

import com.loopers.annotation.SpringE2ETest;
import com.loopers.domain.brand.Brand;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.error.CommonErrorType;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import static org.assertj.core.api.Assertions.assertThat;

@SpringE2ETest
@RequiredArgsConstructor
class BrandV1ApiE2ETest {

    private static final String BASE_ENDPOINT = "/api/v1/brands";

    private final TestRestTemplate testRestTemplate;
    private final TransactionTemplate transactionTemplate;
    private final TestEntityManager testEntityManager;
    private final DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("GET " + BASE_ENDPOINT + "/{brandId}")
    @Nested
    class GetBrand {

        private static final String REQUEST_URL = BASE_ENDPOINT + "/{brandId}";

        @DisplayName("""
                    브랜드가 없으면,
                    404 응답과 { meta=NOT_FOUND, data=null }를 보낸다.
                """)
        @Test
        void throwException_whenBrandDoesNotExist() {
            // when
            Long brandId = 1L;

            HttpEntity<?> requestEntity = HttpEntity.EMPTY;
            String url = UriComponentsBuilder.fromPath(REQUEST_URL)
                    .buildAndExpand(brandId)
                    .toUriString();

            ResponseEntity<ApiResponse<BrandResponse.GetBrand>> response =
                    testRestTemplate.exchange(url, HttpMethod.GET, requestEntity, new ParameterizedTypeReference<>() {
                    });

            // then
            assertThat(response.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND)).isTrue();
            assertThat(response.getBody().meta().errorCode()).isEqualTo(CommonErrorType.NOT_FOUND.getCode());
            assertThat(response.getBody().data()).isNull();
        }

        @DisplayName("브랜드가 있으면, 브랜드 정보를 보낸다.")
        @Test
        void sendBrandInfo_whenBrandExists() {
            // given
            Brand brand = Brand.builder()
                    .name("Nike")
                    .description("Just Do It.")
                    .build();
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(brand));

            // when
            HttpEntity<?> requestEntity = HttpEntity.EMPTY;
            String url = UriComponentsBuilder.fromPath(REQUEST_URL)
                    .buildAndExpand(brand.getId())
                    .toUriString();

            ResponseEntity<ApiResponse<BrandResponse.GetBrand>> response =
                    testRestTemplate.exchange(url, HttpMethod.GET, requestEntity, new ParameterizedTypeReference<>() {
                    });

            // then
            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(response.getBody().data().getBrandId()).isEqualTo(brand.getId());
            assertThat(response.getBody().data().getBrandName()).isEqualTo(brand.getName());
            assertThat(response.getBody().data().getBrandDescription()).isEqualTo(brand.getDescription());
        }

    }

}
