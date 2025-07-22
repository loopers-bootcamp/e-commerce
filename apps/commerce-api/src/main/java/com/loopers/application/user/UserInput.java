package com.loopers.application.user;

import com.loopers.domain.user.UserCommand;
import com.loopers.domain.user.attribute.Email;
import com.loopers.domain.user.attribute.Gender;
import lombok.*;

import java.time.LocalDate;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserInput {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Join {
        private final String userName;
        private final Gender gender;
        private final LocalDate birthDate;
        private final Email email;

        public UserCommand.Join toCommand() {
            return UserCommand.Join.builder()
                    .userName(this.userName)
                    .gender(this.gender)
                    .birthDate(this.birthDate)
                    .email(this.email)
                    .build();
        }
    }

}
