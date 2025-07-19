package com.loopers.domain.user;

import com.loopers.config.jpa.converter.EmailConverter;
import com.loopers.config.jpa.converter.GenderConverter;
import com.loopers.domain.BaseEntity;
import com.loopers.domain.user.attribute.Email;
import com.loopers.domain.user.attribute.Gender;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.regex.Pattern;

@Getter
@Entity
@Table(name = "user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    /**
     * 아이디
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false, updatable = false)
    private Long id;

    /**
     * 이름
     */
    @Column(name = "user_name", nullable = false, unique = true)
    private String name;

    /**
     * 성별
     */
    @Convert(converter = GenderConverter.class)
    @Column(name = "gender", nullable = false)
    private Gender gender;

    /**
     * 생년월일
     */
    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    /**
     * 이메일
     */
    @Convert(converter = EmailConverter.class)
    @Column(name = "email", nullable = false)
    private Email email;

    // -------------------------------------------------------------------------------------------------

    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\d]{1,10}$");
    private static final Pattern BIRTH_DATE_PATTERN = Pattern.compile("^(?<year>\\d{4})-(?<month>0[1-9]|1[0-2])-(?<day>0[1-9]|[12]\\d|3[01])$");

    @Builder
    private User(String name, Integer genderCode, String birthDate, String email) {
        if (!StringUtils.hasText(name) || !NAME_PATTERN.matcher(name).matches()) {
            throw new BusinessException(CommonErrorType.INVALID,
                    "이름은 영문 및 숫자로 10자 이내여야 합니다.");
        }

        Gender gender = Gender.from(genderCode);
        if (gender == null) {
            throw new BusinessException(CommonErrorType.INVALID,
                    "올바르지 않은 성별입니다.");
        }

        if (!StringUtils.hasText(birthDate) || !BIRTH_DATE_PATTERN.matcher(birthDate).matches()) {
            throw new BusinessException(CommonErrorType.INVALID,
                    "생년월일 형식이 올바르지 않습니다.");
        }

        if (!Email.isValid(email)) {
            throw new BusinessException(CommonErrorType.INVALID,
                    "이메일 형식이 올바르지 않습니다.");
        }

        this.name = name;
        this.gender = gender;
        this.birthDate = LocalDate.parse(birthDate);
        this.email = new Email(email);
    }

}
