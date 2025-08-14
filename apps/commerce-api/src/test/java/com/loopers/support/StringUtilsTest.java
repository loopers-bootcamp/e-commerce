package com.loopers.support;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class StringUtilsTest {

    @DisplayName("각 자릿수마다 9의 보수로 변환하고, 재변환 시 원래 값으로 되돌아간다.")
    @CsvSource(textBlock = """
            0000000000000000000 | 9999999999999999999
            0000000000000000001 | 9999999999999999998
            1234567890123456789 | 8765432109876543210
            9223372036854775806 | 0776627963145224193
            9223372036854775807 | 0776627963145224192
            9999999999999999998 | 0000000000000000001
            9999999999999999999 | 0000000000000000000
            """, delimiter = '|')
    @ParameterizedTest
    void returnInvertedStringEach9sComplement(String src, String expected) {
        // when
        String inverted = StringUtils.invert9sComplement(src);

        // then
        assertThat(inverted).isEqualTo(expected);

        // when
        String reinverted = StringUtils.invert9sComplement(inverted);

        // then
        assertThat(reinverted).isEqualTo(src);
    }

}
