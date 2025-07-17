package com.loopers.application.user;

import com.loopers.domain.user.UserCommand;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserInput {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Join {
        private final String userName;
        private final Integer genderCode;
        private final String birthDate;
        private final String email;

        public UserCommand.Join toCommand() {
            return UserCommand.Join.builder()
                    .userName(this.userName)
                    .genderCode(this.genderCode)
                    .birthDate(this.birthDate)
                    .email(this.email)
                    .build();
        }
    }

}
