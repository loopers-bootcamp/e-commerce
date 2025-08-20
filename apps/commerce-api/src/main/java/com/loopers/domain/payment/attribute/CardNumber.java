package com.loopers.domain.payment.attribute;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

@Getter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class CardNumber {

    private static final Pattern PATTERN = Pattern.compile("^\\d{16}$");

    @JsonValue
    @EqualsAndHashCode.Include
    private final String value;

    // -------------------------------------------------------------------------------------------------

    @JsonCreator
    public CardNumber(String value) {
        if (!isValid(value)) {
            throw new BusinessException(CommonErrorType.INVALID, "카드번호 형식이 올바르지 않습니다.");
        }

        this.value = value;
    }

    // -------------------------------------------------------------------------------------------------

    public static boolean isValid(String value) {
        return StringUtils.hasText(value) && PATTERN.matcher(value).matches();
    }

    public String toFormattedString() {
        return this.value.replaceAll("(?<=\\d{4})(\\d{4})", "-$1");
    }

}
