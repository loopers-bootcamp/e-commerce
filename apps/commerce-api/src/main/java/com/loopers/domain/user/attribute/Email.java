package com.loopers.domain.user.attribute;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.regex.Pattern;

@Getter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class Email {

    /**
     * OWASP Validation Regex Repository: E-Mail
     *
     * @see <a href="https://owasp.org/www-community/OWASP_Validation_Regex_Repository">OWASP</a>
     */
    public static final String PATTERN_VALUE = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

    private static final Pattern PATTERN = Pattern.compile(PATTERN_VALUE);

    @JsonValue
    @EqualsAndHashCode.Include
    private final String value;

    // -------------------------------------------------------------------------------------------------

    @JsonCreator
    public Email(String value) {
        if (!isValid(value)) {
            throw new BusinessException(CommonErrorType.INVALID, "이메일 형식이 올바르지 않습니다.");
        }

        this.value = value.toLowerCase(Locale.ROOT);
    }

    // -------------------------------------------------------------------------------------------------

    public static boolean isValid(String value) {
        return StringUtils.hasText(value) && PATTERN.matcher(value).matches();
    }

    public String getLocalPart() {
        return this.value.substring(0, this.value.indexOf('@'));
    }

    public String getDomain() {
        return this.value.substring(this.value.indexOf('@') + 1);
    }

}
