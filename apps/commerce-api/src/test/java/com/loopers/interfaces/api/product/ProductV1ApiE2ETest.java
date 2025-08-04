package com.loopers.interfaces.api.product;

import com.loopers.annotation.SpringE2ETest;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductOption;
import com.loopers.domain.product.Stock;
import com.loopers.domain.product.attribute.ProductSearchSortType;
import com.loopers.domain.user.User;
import com.loopers.domain.user.attribute.Email;
import com.loopers.domain.user.attribute.Gender;
import com.loopers.interfaces.api.ApiHeader;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.utils.DatabaseCleanUp;
import lombok.RequiredArgsConstructor;
import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.root;

@SpringE2ETest
@RequiredArgsConstructor
class ProductV1ApiE2ETest {

    private static final String BASE_ENDPOINT = "/api/v1/products";

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
    class SearchProducts {

        private static final String REQUEST_URL = BASE_ENDPOINT;

        @DisplayName("페이지 번호와 크기에 따른 페이징된 상품 목록 정보를 보낸다.")
        @CsvSource(textBlock = """
                19 | 0 | 10 | 2 | 10
                20 | 0 | 10 | 2 | 10
                21 | 0 | 10 | 3 | 10
                5  | 4 | 1  | 5 | 1
                5  | 5 | 1  | 5 | 0
                11 | 0 | 10 | 2 | 10
                11 | 1 | 10 | 2 | 1
                11 | 2 | 10 | 2 | 0
                """, delimiter = '|')
        @ParameterizedTest
        void sendPagedProducts_withPageNumberAndPageSize(int totalElements, int page, int size, int expectedTotalPages, int expectedItemSize) {
            // given
            List<Product> products = IntStream.range(0, totalElements)
                    .mapToObj(i -> Product.builder()
                            .name(Instancio.of(String.class).generate(root(),
                                            gen -> gen.string().alphaNumeric().mixedCase().length(10, 50))
                                    .create()
                            )
                            .basePrice((i + 1) * 1000)
                            .brandId(null)
                            .build()
                    )
                    .toList();
            transactionTemplate.executeWithoutResult(status -> products.forEach(testEntityManager::persist));

            ProductRequest.SearchProducts request = ProductRequest.SearchProducts.builder()
                    .keyword(null)
                    .brandId(null)
                    .sort(ProductSearchSortType.LATEST)
                    .page(page)
                    .size(size)
                    .build();

            // when
            HttpEntity<?> requestEntity = HttpEntity.EMPTY;
            String url = UriComponentsBuilder.fromPath(REQUEST_URL)
                    .queryParam("keyword", request.getKeyword())
                    .queryParam("brandId", request.getBrandId())
                    .queryParam("sort", request.getSort())
                    .queryParam("page", request.getPage())
                    .queryParam("size", request.getSize())
                    .buildAndExpand()
                    .toUriString();

            ResponseEntity<ApiResponse<ProductResponse.SearchProducts>> response =
                    testRestTemplate.exchange(url, HttpMethod.GET, requestEntity, new ParameterizedTypeReference<>() {
                    });

            // then
            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(response.getBody().data().getTotalPages()).isEqualTo(expectedTotalPages);
            assertThat(response.getBody().data().getTotalItems()).isEqualTo(totalElements);
            assertThat(response.getBody().data().getPage()).isEqualTo(page);
            assertThat(response.getBody().data().getSize()).isEqualTo(size);
            assertThat(response.getBody().data().getItems()).hasSize(expectedItemSize);
        }

    }

    // -------------------------------------------------------------------------------------------------

    @DisplayName("GET " + BASE_ENDPOINT + "/{productId}")
    @Nested
    class GetProductDetail {

        private static final String REQUEST_URL = BASE_ENDPOINT + "/{productId}";

        @DisplayName("상품 아이디와 일치하는 상품이 있으면, 상품 상세 정보를 보낸다.")
        @Test
        void sendProductDetail_whenProductExistsById() {
            // given
            User user = User.builder()
                    .name("gildong")
                    .gender(Gender.MALE)
                    .birthDate(LocalDate.of(1990, 1, 1))
                    .email(new Email("gildong.hong@example.com"))
                    .build();
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(user));

            Product product = Product.builder()
                    .name(Instancio.of(String.class).generate(root(),
                                    gen -> gen.string().alphaNumeric().mixedCase().length(10, 50))
                            .create()
                    )
                    .basePrice(250_000)
                    .brandId(null)
                    .build();
            transactionTemplate.executeWithoutResult(status -> testEntityManager.persist(product));

            List<ProductOption> options = IntStream.range(0, 10)
                    .mapToObj(i -> ProductOption.builder()
                            .name(Instancio.of(String.class).generate(root(),
                                            gen -> gen.string().alphaNumeric().mixedCase().length(5, 12))
                                    .create()
                            )
                            .additionalPrice((i + 1) * 1000)
                            .productId(product.getId())
                            .build()
                    )
                    .toList();
            transactionTemplate.executeWithoutResult(status -> options.forEach(testEntityManager::persist));

            List<Stock> stocks = IntStream.range(0, options.size())
                    .mapToObj(i -> Stock.builder()
                            .productOptionId(options.get(i).getId())
                            .quantity((i + 1) * 10)
                            .build()
                    )
                    .toList();
            transactionTemplate.executeWithoutResult(status -> stocks.forEach(testEntityManager::persist));

            // when
            HttpHeaders headers = new HttpHeaders();
            headers.set(ApiHeader.USER_ID, user.getName());
            HttpEntity<Object> requestEntity = new HttpEntity<>(headers);

            String url = UriComponentsBuilder.fromPath(REQUEST_URL)
                    .buildAndExpand(product.getId())
                    .toUriString();

            ResponseEntity<ApiResponse<ProductResponse.GetProductDetail>> response =
                    testRestTemplate.exchange(url, HttpMethod.GET, requestEntity, new ParameterizedTypeReference<>() {
                    });

            // then
            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(response.getBody().data().getProductId()).isEqualTo(product.getId());
            assertThat(response.getBody().data().getProductName()).isEqualTo(product.getName());
            assertThat(response.getBody().data().getBasePrice()).isEqualTo(product.getBasePrice());
            assertThat(response.getBody().data().getLikeCount()).isEqualTo(0);
            assertThat(response.getBody().data().getOptions()).hasSameSizeAs(options);
            assertThat(response.getBody().data().getOptions())
                    .allMatch(option -> option.getStockQuantity() >= 0);
        }

    }

}
