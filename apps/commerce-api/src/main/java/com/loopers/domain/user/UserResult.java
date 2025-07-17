package com.loopers.domain.user;

import lombok.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserResult {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class GetUser {
        private final Long userId;
        private final String userName;
        private final Integer genderCode;
        private final String birthDate;
        private final String email;

        public static GetUser from(User user) {
            return GetUser.builder()
                    .userId(user.getId())
                    .userName(user.getName())
                    .genderCode(user.getGender().getCode())
                    .birthDate(user.getBirthDate().toString())
                    .email(user.getEmail().getValue())
                    .build();
        }
    }

    // -------------------------------------------------------------------------------------------------

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Join {
        private final Long userId;
        private final String userName;
        private final Integer genderCode;
        private final String birthDate;
        private final String email;

        public static Join from(User user) {
            return Join.builder()
                    .userId(user.getId())
                    .userName(user.getName())
                    .genderCode(user.getGender().getCode())
                    .birthDate(user.getBirthDate().toString())
                    .email(user.getEmail().getValue())
                    .build();
        }
    }

}
