package com.loopers.domain.order.attribute;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;

class OrderIdTest {

    @DisplayName("올바른 형식의 주문 아이디면 true를, 아니면 false를 반환한다.")
    @CsvSource(textBlock = """
            # invalid format
                                                 | false
            ''                                   | false
            ' '                                  | false
            6e2a911f93dc4e01a9370665d34118de     | false
            e425c3f326862a4aa3b8d10c81c2400a9179 | false
            b7a143fv-18fw-43ex-acby-7ac44dcf712z | false
            00000000-0000-0000-0000-000000000000 | false
            ffffffff-ffff-ffff-ffff-ffffffffffff | false
            # after than now
            7fffffff-ffff-fffe-7fff-ffffffffffff | false
            ffffffff-ffff-7fff-bfff-ffffffffffff | false
            # valid
            00000000-0000-1000-8000-000000000000 | true
            01984fbe-bebf-7650-bf67-7d5d3068bfed | true
            01984fbe-bed1-7717-8313-49cc064f9281 | true
            01984fbe-bed1-7b11-9319-39fae3d51d33 | true
            01984feb-c85e-77dd-966b-0a4b73b1708a | true
            """, delimiter = '|')
    @ParameterizedTest
    void checkIfOrderIdIsValid(String uuid, boolean expected) {
        // when
        boolean valid = OrderId.isValid(uuid);

        // then
        assertThat(valid).isEqualTo(expected);
    }

    @DisplayName("같은 값을 가진 주문 아이디는 동등성을 보장한다.")
    @ValueSource(strings = {
            "00000000-0000-1000-8000-000000000000",
            "01984fbe-bebf-7650-bf67-7d5d3068bfed",
            "01984fbe-bed1-7717-8313-49cc064f9281",
            "01984fbe-bed1-7b11-9319-39fae3d51d33",
            "01984feb-c85e-77dd-966b-0a4b73b1708a",
    })
    @ParameterizedTest
    void guaranteeEqualityForEachOther(String uuid) {
        // when
        OrderId orderId = new OrderId(uuid);

        // then
        OrderId same = new OrderId(uuid);
        assertThat(orderId).isEqualTo(same);
        assertThat(orderId.hashCode()).isEqualTo(same.hashCode());

        OrderId other = new OrderId();
        assertThat(orderId).isNotEqualTo(other);
        assertThat(orderId.hashCode()).isNotEqualTo(other.hashCode());
    }

    @DisplayName("각각의 주문 아이디는 찰나에도 유일성을 보장한다.")
    @RepeatedTest(10)
    void guaranteeUniquenessForEachOtherInTheBlinkOfAnEye() {
        // given
        int size = 1000;

        // when
        Set<OrderId> orderIds = IntStream.range(0, size)
                .mapToObj(i -> new OrderId())
                .collect(toSet());

        // then
        assertThat(orderIds).hasSize(size);
    }

    @DisplayName("주문 아이디는 현재와 같거나 과거인 UNIX 타임스탬프를 반환한다.")
    @RepeatedTest(10)
    void returnUnixTimestampThatIsPastOrEqualToCurrentTimestamp() {
        // when
        OrderId orderId = new OrderId();

        // then
        long now = System.currentTimeMillis();
        assertThat(orderId.getTimestamp()).isLessThanOrEqualTo(now);
    }

}
