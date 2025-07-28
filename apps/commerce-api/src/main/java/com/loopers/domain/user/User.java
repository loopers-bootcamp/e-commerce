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
@Table(name = "users")
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

    @Builder
    private User(String name, Gender gender, LocalDate birthDate, Email email) {
        if (!StringUtils.hasText(name) || !NAME_PATTERN.matcher(name).matches()) {
            throw new BusinessException(CommonErrorType.INVALID,
                    "이름은 영문 및 숫자로 10자 이내여야 합니다.");
        }

        if (gender == null) {
            throw new BusinessException(CommonErrorType.INVALID,
                    "올바르지 않은 성별입니다.");
        }

        if (birthDate == null) {
            throw new BusinessException(CommonErrorType.INVALID,
                    "올바르지 않은 생년월일입니다.");
        }

        if (email == null) {
            throw new BusinessException(CommonErrorType.INVALID,
                    "올바르지 않은 이메일입니다.");
        }

        this.name = name;
        this.gender = gender;
        this.birthDate = birthDate;
        this.email = email;
    }

}
