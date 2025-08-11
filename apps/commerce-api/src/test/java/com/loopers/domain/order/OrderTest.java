package com.loopers.domain.order;

import com.loopers.domain.order.attribute.OrderStatus;
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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.instancio.Select.field;

class OrderTest {

    @DisplayName("주문을 생성할 때:")
    @Nested
    class Create {

        @DisplayName("주문 아이디가 null이면, BusinessException이 발생한다.")
        @Test
        void throwException_whenIdIsNull() {
            // given
            Long totalPrice = 10000L;
            Long userId = 1L;

            // when & then
            assertThatException()
                    .isThrownBy(() -> Order.builder()
                            .id(null)
                            .totalPrice(totalPrice)
                            .userId(userId)
                            .build()
                    )
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorType", CommonErrorType.INVALID);
        }

        @DisplayName("주문 아이디의 타임스탬프가 현재 시간보다 미래이면, BusinessException이 발생한다.")
        @Test
        void throwException_whenIdTimestampIsInFuture() {
            // given
            long futureTimestampMs = Instant.now().plus(1L, ChronoUnit.DAYS).toEpochMilli();
            UUID futureId = createTimeBasedUuid(futureTimestampMs);

            Long totalPrice = 10000L;
            Long userId = 1L;

            // when & then
            assertThatException()
                    .isThrownBy(() -> Order.builder()
                            .id(futureId)
                            .totalPrice(totalPrice)
                            .userId(userId)
                            .build()
                    )
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorType", CommonErrorType.INVALID);
        }

        @DisplayName("총 가격이 유효하지 않으면, BusinessException이 발생한다.")
        @NullSource
        @ValueSource(longs = {
                Long.MIN_VALUE, -100, -1,
        })
        @ParameterizedTest
        void throwException_withInvalidTotalPrice(Long totalPrice) {
            // given
            UUID id = createTimeBasedUuid();
            Long userId = 1L;

            // when & then
            assertThatException()
                    .isThrownBy(() -> Order.builder()
                            .id(id)
                            .totalPrice(totalPrice)
                            .userId(userId)
                            .build()
                    )
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorType", CommonErrorType.INVALID);
        }

        @DisplayName("사용자 아이디가 유효하지 않으면, BusinessException이 발생한다.")
        @NullSource
        @ParameterizedTest
        void throwException_withInvalidUserId(Long userId) {
            // given
            UUID id = createTimeBasedUuid();
            Long totalPrice = 10000L;

            // when & then
            assertThatException()
                    .isThrownBy(() -> Order.builder()
                            .id(id)
                            .totalPrice(totalPrice)
                            .userId(userId)
                            .build()
                    )
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorType", CommonErrorType.INVALID);
        }

        @DisplayName("유효한 값이면, 주문을 생성하고 상태는 CREATED로 설정된다.")
        @CsvSource(textBlock = """
                00000000-0000-1000-8000-000000000000 | 1000    | 1
                01984fbe-bebf-7650-bf67-7d5d3068bfed | 10_000  | 1
                01984fbe-bed1-7717-8313-49cc064f9281 | 50_000  | 20
                01984fbe-bed1-7b11-9319-39fae3d51d33 | 999_990 | 2
                01984feb-c85e-77dd-966b-0a4b73b1708a | 0       | 300
                """, delimiter = '|')
        @ParameterizedTest
        void createNewOrder_withValidValues(UUID id, Long totalPrice, Long userId) {
            // when
            Order order = Order.builder()
                    .id(id)
                    .totalPrice(totalPrice)
                    .discountAmount(0)
                    .userId(userId)
                    .build();

            // then
            assertThat(order).isNotNull();
            assertThat(order.getId()).isEqualTo(id);
            assertThat(order.getTotalPrice()).isEqualTo(totalPrice);
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
            assertThat(order.getUserId()).isEqualTo(userId);
            assertThat(order.getProducts()).isEmpty();
        }

    }

    // -------------------------------------------------------------------------------------------------

    @DisplayName("주문 상품을 추가할 때:")
    @Nested
    class AddProducts {

        @DisplayName("빈 주문 상품 목록이 주어지면, 기존 주문 상품 목록에 변화가 없다.")
        @Test
        void noChange_withEmptyProducts() {
            // given
            Order order = Instancio.of(Order.class)
                    .set(field(Order::getProducts), List.of())
                    .create();

            // when
            order.addProducts(List.of());

            // then
            assertThat(order.getProducts()).hasSize(0);
        }

        @DisplayName("중복된 주문 상품 목록이 주어지면, BusinessException이 발생한다.")
        @Test
        void throwException_whenDuplicatedProductIdsAreProvided() {
            // given
            UUID orderId = createTimeBasedUuid();

            Order order = Instancio.of(Order.class)
                    .set(field(Order::getId), orderId)
                    .set(field(Order::getProducts), List.of())
                    .create();

            List<OrderProduct> products = IntStream.range(0, 10)
                    .mapToObj(i -> Instancio.of(OrderProduct.class)
                            .set(field(OrderProduct::getId), 100L)
                            .set(field(OrderProduct::getOrderId), orderId)
                            .create()
                    )
                    .toList();

            // when & then
            assertThatException()
                    .isThrownBy(() -> order.addProducts(products))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorType", CommonErrorType.CONFLICT);
        }

        @DisplayName("주문 아이디가 현재 주문의 아이디와 일치하지 않으면, BusinessException이 발생한다.")
        @CsvSource(textBlock = """
                                                     | 01984fbe-bed1-7717-8313-49cc064f9281
                01984fbe-bebf-7650-bf67-7d5d3068bfed |
                01984fbe-bebf-7650-bf67-7d5d3068bfed | 01984fbe-bed1-7717-8313-49cc064f9281
                """, delimiter = '|')
        @ParameterizedTest
        void throwException_withInconsistentOrderId(UUID orderId, UUID otherOrderId) {
            // given
            Order order = Instancio.of(Order.class)
                    .set(field(Order::getId), orderId)
                    .set(field(Order::getProducts), List.of())
                    .create();

            List<OrderProduct> products = IntStream.range(0, 10)
                    .mapToObj(i -> Instancio.of(OrderProduct.class)
                            .set(field(OrderProduct::getId), i + 1L)
                            .set(field(OrderProduct::getOrderId), otherOrderId)
                            .create()
                    )
                    .toList();

            // when & then
            assertThatException()
                    .isThrownBy(() -> order.addProducts(products))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorType", CommonErrorType.INCONSISTENT);
        }

        @DisplayName("이미 추가된 주문 상품이 주어지면, 추가하지 않는다.")
        @Test
        void ignoreOptions_whenProductsAlreadyAddedAreProvided() {
            // given
            UUID orderId = createTimeBasedUuid();

            Order order = Instancio.of(Order.class)
                    .set(field(Order::getId), orderId)
                    .set(field(Order::getProducts),
                            IntStream.range(0, 10)
                                    .mapToObj(i -> Instancio.of(OrderProduct.class)
                                            .set(field(OrderProduct::getId), i + 1L)
                                            .set(field(OrderProduct::getOrderId), orderId)
                                            .create()
                                    )
                                    .toList()
                    )
                    .create();

            List<OrderProduct> products = IntStream.range(5, 15)
                    .mapToObj(i -> Instancio.of(OrderProduct.class)
                            .set(field(OrderProduct::getId), i + 1L)
                            .set(field(OrderProduct::getOrderId), orderId)
                            .create()
                    )
                    .toList();

            // when
            order.addProducts(products);

            // then
            assertThat(order.getProducts()).hasSize(15);
        }

        @DisplayName("주문 상품 아이디가 없어도, 주문 상품 목록을 추가할 수 있다.")
        @Test
        void addProducts_whenEachProductDoesNotHaveId() {
            // given
            UUID orderId = createTimeBasedUuid();

            Order order = Instancio.of(Order.class)
                    .set(field(Order::getId), orderId)
                    .set(field(Order::getProducts),
                            IntStream.range(0, 10)
                                    .mapToObj(i -> Instancio.of(OrderProduct.class)
                                            .set(field(OrderProduct::getId), i + 1L)
                                            .set(field(OrderProduct::getOrderId), orderId)
                                            .create()
                                    )
                                    .toList()
                    )
                    .create();

            List<OrderProduct> products = IntStream.range(0, 10)
                    .mapToObj(i -> Instancio.of(OrderProduct.class)
                            .ignore(field(OrderProduct::getId))
                            .set(field(OrderProduct::getOrderId), orderId)
                            .create()
                    )
                    .toList();

            // when
            order.addProducts(products);

            // then
            assertThat(order.getProducts()).hasSize(20);
        }

        @DisplayName("유일한 주문 상품 아이디와 동일한 주문 아이디가 있으면, 주문 상품 목록을 추가할 수 있다.")
        @Test
        void addProducts_whenEachProductHasUniqueIdAndSameOrderId() {
            // given
            UUID orderId = createTimeBasedUuid();

            Order order = Instancio.of(Order.class)
                    .set(field(Order::getId), orderId)
                    .set(field(Order::getProducts),
                            IntStream.range(0, 10)
                                    .mapToObj(i -> Instancio.of(OrderProduct.class)
                                            .set(field(OrderProduct::getId), i + 1L)
                                            .set(field(OrderProduct::getOrderId), orderId)
                                            .create()
                                    )
                                    .toList()
                    )
                    .create();

            List<OrderProduct> products = IntStream.range(0, 10)
                    .mapToObj(i -> Instancio.of(OrderProduct.class)
                            .set(field(OrderProduct::getId), i + 100L)
                            .set(field(OrderProduct::getOrderId), orderId)
                            .create()
                    )
                    .toList();

            // when
            order.addProducts(products);

            // then
            assertThat(order.getProducts()).hasSize(20);
        }

    }

    // -------------------------------------------------------------------------------------------------

    @DisplayName("주문 상태를 변경할 때:")
    @Nested
    class ChangeStatus {

        @DisplayName("주문이 완료되면, 상태가 COMPLETE로 변경된다.")
        @Test
        void changeStatusToComplete() {
            // given
            Order order = Instancio.of(Order.class)
                    .set(field(Order::getStatus), OrderStatus.CREATED)
                    .create();

            // when
            order.complete();

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETE);
        }

        @DisplayName("주문이 만료되면, 상태가 EXPIRED로 변경된다.")
        @Test
        void changeStatusToExpired() {
            // given
            Order order = Instancio.of(Order.class)
                    .set(field(Order::getStatus), OrderStatus.CREATED)
                    .create();

            // when
            order.expire();

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.EXPIRED);
        }

        @DisplayName("주문이 취소되면, 상태가 CANCELED로 변경된다.")
        @Test
        void changeStatusToCanceled() {
            // given
            Order order = Instancio.of(Order.class)
                    .set(field(Order::getStatus), OrderStatus.CREATED)
                    .create();

            // when
            order.cancel();

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);
        }

    }

    // -------------------------------------------------------------------------------------------------

    @DisplayName("타임스탬프로 변환할 때:")
    @Nested
    class ToTimestamp {

        @DisplayName("주문 아이디에서 정확한 Unix epoch milliseconds를 추출한다.")
        @Test
        void extractCorrectTimestampFromId() {
            // given
            UUID id = createTimeBasedUuid();

            Order order = Instancio.of(Order.class)
                    .set(field(Order::getId), id)
                    .create();

            // when
            long actualTimestamp = order.toTimestamp();

            // then
            long expectedTimestamp = (id.getMostSignificantBits() >>> 16) & 0xFFFFFFFFFFFFL;
            assertThat(actualTimestamp).isEqualTo(expectedTimestamp);
        }

    }

    // -------------------------------------------------------------------------------------------------

    @DisplayName("주문 객체의 동등성을 비교할 때:")
    @Nested
    class EqualsAndHashCode {

        @DisplayName("같은 값을 가진 주문 아이디는 동등성을 보장한다.")
        @ValueSource(strings = {
                "00000000-0000-1000-8000-000000000000",
                "01984fbe-bebf-7650-bf67-7d5d3068bfed",
                "01984fbe-bed1-7717-8313-49cc064f9281",
                "01984fbe-bed1-7b11-9319-39fae3d51d33",
                "01984feb-c85e-77dd-966b-0a4b73b1708a",
        })
        @ParameterizedTest
        void guaranteeEqualityForEachOther(UUID id) {
            // given
            Order order = Instancio.of(Order.class)
                    .set(field(Order::getId), id)
                    .create();
            Order other = Instancio.of(Order.class)
                    .set(field(Order::getId), id)
                    .create();

            // when & then
            assertThat(order).isEqualTo(other);
            assertThat(other).isEqualTo(order);
            assertThat(order.hashCode()).isEqualTo(other.hashCode());
            assertThat(other.hashCode()).isEqualTo(order.hashCode());
        }

    }

    // -------------------------------------------------------------------------------------------------

    @DisplayName("주문 객체를 비교할 때:")
    @Nested
    class CompareTo {

        @DisplayName("타임스탬프가 다르면 타임스탬프 기준으로 비교한다.")
        @Test
        void compareByTimestampWhenTimestampsAreDifferent() {
            // given
            long t1 = System.currentTimeMillis() - 1000;
            long t2 = System.currentTimeMillis();
            long t3 = System.currentTimeMillis() + 1000;

            UUID id1 = createTimeBasedUuid(t1);
            UUID id2 = createTimeBasedUuid(t2);
            UUID id3 = createTimeBasedUuid(t3);

            Order order1 = Instancio.of(Order.class).set(field(Order::getId), id1).create();
            Order order2 = Instancio.of(Order.class).set(field(Order::getId), id2).create();
            Order order3 = Instancio.of(Order.class).set(field(Order::getId), id3).create();

            // when & then
            assertThat(order1).isLessThan(order2);
            assertThat(order2).isLessThan(order3);
            assertThat(order1).isLessThan(order2);
            assertThat(order3).isGreaterThan(order2);
            assertThat(order2).isGreaterThan(order1);

            assertThat(order1).isEqualByComparingTo(order1);
            assertThat(order2).isEqualByComparingTo(order2);
            assertThat(order3).isEqualByComparingTo(order3);
        }

    }

    // -------------------------------------------------------------------------------------------------

    private static UUID createTimeBasedUuid() {
        return createTimeBasedUuid(System.currentTimeMillis());
    }

    private static UUID createTimeBasedUuid(long timestampMs) {
        return createTimeBasedUuid(timestampMs, Instancio.create(Long.class));
    }

    private static UUID createTimeBasedUuid(long timestampMs, long lsb) {
        long msb = (timestampMs & 0xFFFFFFFFFFFFL) << 16;
        msb |= (7L << 12);
        msb |= Instancio.create(Long.class) & 0x0FFF;

        return new UUID(msb, lsb);
    }

}
