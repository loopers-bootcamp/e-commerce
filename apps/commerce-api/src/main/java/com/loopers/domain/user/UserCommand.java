package com.loopers.domain.user;

import com.loopers.domain.user.attribute.Email;
import com.loopers.domain.user.attribute.Gender;
import lombok.*;

import java.time.LocalDate;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserCommand {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Join {
        private final String userName;
        private final Gender gender;
        private final LocalDate birthDate;
        private final Email email;

        public User toEntity() {
            return User.builder()
                    .name(this.userName)
                    .gender(this.gender)
                    .birthDate(this.birthDate)
                    .email(this.email)
                    .build();
        }
    }

}
