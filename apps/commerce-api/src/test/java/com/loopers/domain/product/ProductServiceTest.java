package com.loopers.domain.product;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@MockitoSettings
class ProductServiceTest {

    @InjectMocks
    private ProductService sut;

    @Mock
    private ProductRepository productRepository;

    @DisplayName("상품 상세를 조회할 때:")
    @Nested
    class GetProductDetail {

        @DisplayName("상품 아이디와 일치하는 상품이 없으면, Optional.empty를 반환한다.")
        @Test
        void returnEmptyOptional_whenProductDoesNotExistById() {
            // given
            Long productId = 1L;
            given(productRepository.findProductDetailById(productId))
                    .willReturn(Optional.empty());

            // when
            Optional<ProductResult.GetProductDetail> maybeDetail = sut.getProductDetail(productId);

            // then
            assertThat(maybeDetail).isEmpty();
            verify(productRepository).findProductDetailById(productId);
        }

        @DisplayName("상품 아이디와 일치하는 상품이 있으면, 상품 상세 정보를 반환한다.")
        @Test
        void returnProductDetail_whenProductExistsById() {
            // given
            Long productId = 1L;
            given(productRepository.findProductDetailById(productId))
                    .willReturn(Optional.of(
                            ProductQueryResult.ProductDetail.builder()
                                    .options(List.of())
                                    .build()
                    ));

            // when
            Optional<ProductResult.GetProductDetail> maybeDetail = sut.getProductDetail(productId);

            // then
            assertThat(maybeDetail).isPresent();
            verify(productRepository).findProductDetailById(productId);
        }

    }

    // -------------------------------------------------------------------------------------------------

}
