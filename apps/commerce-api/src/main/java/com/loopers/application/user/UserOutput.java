package com.loopers.application.user;

import com.loopers.domain.user.UserResult;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserOutput {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class GetUser {
        private final Long userId;
        private final String userName;
        private final Integer genderCode;
        private final String birthDate;
        private final String email;

        public static GetUser from(UserResult.GetUser result) {
            return GetUser.builder()
                    .userId(result.getUserId())
                    .userName(result.getUserName())
                    .genderCode(result.getGenderCode())
                    .birthDate(result.getBirthDate())
                    .email(result.getEmail())
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

        public static Join from(UserResult.Join result) {
            return Join.builder()
                    .userId(result.getUserId())
                    .userName(result.getUserName())
                    .genderCode(result.getGenderCode())
                    .birthDate(result.getBirthDate())
                    .email(result.getEmail())
                    .build();
        }
    }

}
