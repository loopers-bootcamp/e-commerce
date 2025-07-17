package com.loopers.domain.user;

import lombok.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserCommand {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Join {
        private final String userName;
        private final Integer genderCode;
        private final String birthDate;
        private final String email;

        public User toEntity() {
            return User.builder()
                    .name(this.userName)
                    .genderCode(this.genderCode)
                    .birthDate(this.birthDate)
                    .email(this.email)
                    .build();
        }
    }

}
