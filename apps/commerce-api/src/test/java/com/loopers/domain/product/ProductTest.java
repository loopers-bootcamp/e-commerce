package com.loopers.domain.product;

import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.instancio.Select.field;

class ProductTest {

    @DisplayName("상품을 생성할 때:")
    @Nested
    class Create {

        @DisplayName("이름이 유효하지 않으면, BusinessException이 발생한다.")
        @NullSource
        @ValueSource(strings = {
                "", " ",
        })
        @ParameterizedTest
        void throwException_withInvalidName(String name) {
            // given
            Integer basePrice = 10000;

            // when & then
            assertThatException()
                    .isThrownBy(() -> Product.builder()
                            .name(name)
                            .basePrice(basePrice)
                            .build()
                    )
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorType", CommonErrorType.INVALID);
        }

        @DisplayName("기본 가격이 유효하지 않으면, BusinessException이 발생한다.")
        @NullSource
        @ValueSource(ints = {
                Integer.MIN_VALUE, -10000, -500, -1,
        })
        @ParameterizedTest
        void throwException_withInvalidBasePrice(Integer basePrice) {
            // given
            String name = "Foo Bar";

            // when & then
            assertThatException()
                    .isThrownBy(() -> Product.builder()
                            .name(name)
                            .basePrice(basePrice)
                            .build()
                    )
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorType", CommonErrorType.INVALID);
        }

        @DisplayName("유효한 값이면, 상품을 생성한다.")
        @CsvSource(textBlock = """
                Foo            | 0       |
                Foo Bar (no.1) | 50_000  | 1
                Foo/Bar + Qux  | 100_000 | 100
                """, delimiter = '|')
        @ParameterizedTest
        void createNewProduct_withValidValues(String name, Integer basePrice, Long brandId) {
            // when
            Product product = Product.builder()
                    .name(name)
                    .basePrice(basePrice)
                    .brandId(brandId)
                    .build();

            // then
            assertThat(product).isNotNull();
            assertThat(product.getName()).isEqualTo(name);
            assertThat(product.getBasePrice()).isEqualTo(basePrice);
        }
    }

    // -------------------------------------------------------------------------------------------------

    @DisplayName("상품이 좋아요 당할 때:")
    @Nested
    class Like {

        @DisplayName("좋아요 수를 1 증가시킨다.")
        @CsvSource(textBlock = """
                0    | 1
                1    | 2
                99   | 100
                9998 | 9999
                """, delimiter = '|')
        @ParameterizedTest
        void increaseLikeCountByOne(long initialLikeCount, long expectedLikeCount) {
            // given
            Product product = Instancio.of(Product.class)
                    .set(field(Product::getLikeCount), initialLikeCount)
                    .create();

            // when
            product.like();

            // then
            assertThat(product.getLikeCount()).isEqualTo(expectedLikeCount);
        }

        @DisplayName("좋아요 수가 최대값에 도달하면, 더 이상 증가하지 않는다.")
        @Test
        void doesNotIncrease_whenLikeCountReachesMaxValue() {
            // given
            long maxLikeCount = Long.MAX_VALUE;
            Product product = Instancio.of(Product.class)
                    .set(field(Product::getLikeCount), maxLikeCount)
                    .create();

            // when
            product.like();

            // then
            assertThat(product.getLikeCount()).isEqualTo(maxLikeCount);
        }

    }

    // -------------------------------------------------------------------------------------------------

    @DisplayName("상품이 싫어요 당할 때:")
    @Nested
    class Dislike {

        @DisplayName("좋아요 수를 1 감소시킨다.")
        @CsvSource(textBlock = """
                1    | 0
                2    | 1
                100  | 99
                9999 | 9998
                """, delimiter = '|')
        @ParameterizedTest
        void increaseLikeCountByOne(long initialLikeCount, long expectedLikeCount) {
            // given
            Product product = Instancio.of(Product.class)
                    .set(field(Product::getLikeCount), initialLikeCount)
                    .create();

            // when
            product.dislike();

            // then
            assertThat(product.getLikeCount()).isEqualTo(expectedLikeCount);
        }

        @DisplayName("좋아요 수가 0에 도달하면, 더 이상 감소하지 않는다.")
        @Test
        void doesNotIncrease_whenLikeCountReachesMaxValue() {
            // given
            long minLikeCount = 0L;
            Product product = Instancio.of(Product.class)
                    .set(field(Product::getLikeCount), minLikeCount)
                    .create();

            // when
            product.dislike();

            // then
            assertThat(product.getLikeCount()).isEqualTo(minLikeCount);
        }

    }

    // -------------------------------------------------------------------------------------------------

    @DisplayName("옵션을 추가할 때:")
    @Nested
    class AddOptions {

        @DisplayName("빈 옵션 목록이 주어지면, 기존 옵션 목록에 변화가 없다.")
        @Test
        void noChange_withEmptyOptions() {
            // given
            Product product = Instancio.of(Product.class)
                    .set(field(Product::getOptions), List.of())
                    .create();

            // when
            product.addOptions(List.of());

            // then
            assertThat(product.getOptions()).hasSize(0);
        }

        @DisplayName("중복된 옵션 목록이 주어지면, BusinessException이 발생한다.")
        @Test
        void throwException_whenDuplicatedOptionsAreProvided() {
            // given
            Product product = Instancio.of(Product.class)
                    .set(field(Product::getOptions), List.of())
                    .create();

            // when & then
            List<ProductOption> options = IntStream.range(0, 10)
                    .mapToObj(i -> Instancio.of(ProductOption.class)
                            .set(field(ProductOption::getId), 100L)
                            .ignore(field(ProductOption::getProductId))
                            .create()
                    )
                    .toList();

            assertThatException()
                    .isThrownBy(() -> product.addOptions(options))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorType", CommonErrorType.CONFLICT);
        }

        @DisplayName("상품 아이디가 현재 상품의 아이디와 일치하지 않으면, BusinessException이 발생한다.")
        @Test
        void throwException_withInconsistentProductId() {
            // given
            Product product = Instancio.of(Product.class)
                    .set(field(Product::getOptions), List.of())
                    .create();

            // when & then
            List<ProductOption> options = IntStream.range(0, 10)
                    .mapToObj(i -> Instancio.of(ProductOption.class)
                            .ignore(field(ProductOption::getId))
                            .set(field(ProductOption::getProductId), product.getId() + 1)
                            .create()
                    )
                    .toList();

            assertThatException()
                    .isThrownBy(() -> product.addOptions(options))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorType", CommonErrorType.INCONSISTENT);
        }

        @DisplayName("이미 추가된 상품이 주어지면, 추가하지 않는다.")
        @Test
        void ignoreOptions_whenProductsAlreadyAddedAreProvided() {
            // given
            Product product = Instancio.of(Product.class)
                    .set(field(Product::getOptions),
                            IntStream.range(0, 10)
                                    .mapToObj(i -> Instancio.of(ProductOption.class)
                                            .set(field(ProductOption::getId), i + 1L)
                                            .ignore(field(ProductOption::getProductId))
                                            .create()
                                    )
                                    .toList()
                    )
                    .create();

            List<ProductOption> options = IntStream.range(5, 15)
                    .mapToObj(i -> Instancio.of(ProductOption.class)
                            .set(field(ProductOption::getId), i + 1L)
                            .ignore(field(ProductOption::getProductId))
                            .create()
                    )
                    .toList();

            // when
            product.addOptions(options);

            // then
            assertThat(product.getOptions()).hasSize(15);
        }

        @DisplayName("옵션 아이디와 상품 아이디가 없어도, 옵션 목록을 추가할 수 있다.")
        @Test
        void addOptions_whenEachOptionDoesNotHaveIdAndProductId() {
            // given
            Product product = Instancio.of(Product.class)
                    .set(field(Product::getOptions),
                            IntStream.range(0, 10)
                                    .mapToObj(i -> Instancio.of(ProductOption.class)
                                            .set(field(ProductOption::getId), i + 1L)
                                            .ignore(field(ProductOption::getProductId))
                                            .create()
                                    )
                                    .toList()
                    )
                    .create();

            List<ProductOption> options = IntStream.range(0, 10)
                    .mapToObj(i -> Instancio.of(ProductOption.class)
                            .ignore(field(ProductOption::getId))
                            .ignore(field(ProductOption::getProductId))
                            .create()
                    )
                    .toList();

            // when
            product.addOptions(options);

            // then
            assertThat(product.getOptions()).hasSize(20);
        }

        @DisplayName("유일한 옵션 아이디와 동일한 상품 아이디가 있으면, 옵션 목록을 추가할 수 있다.")
        @Test
        void addOptions_whenEachOptionHasUniqueIdAndSameProductId() {
            // given
            Product product = Instancio.of(Product.class)
                    .set(field(Product::getOptions),
                            IntStream.range(0, 10)
                                    .mapToObj(i -> Instancio.of(ProductOption.class)
                                            .set(field(ProductOption::getId), i + 1L)
                                            .ignore(field(ProductOption::getProductId))
                                            .create()
                                    )
                                    .toList()
                    )
                    .create();

            // when
            List<ProductOption> options = IntStream.range(0, 10)
                    .mapToObj(i -> Instancio.of(ProductOption.class)
                            .set(field(ProductOption::getId), i + 100L)
                            .set(field(ProductOption::getProductId), product.getId())
                            .create()
                    )
                    .toList();

            product.addOptions(options);

            // then
            assertThat(product.getOptions()).hasSize(20);
        }

    }

}
