package com.loopers.interfaces.api.user;

import com.loopers.application.user.UserOutput;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserResponse {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class GetUser {
        private final Long userId;
        private final String userName;
        private final Integer genderCode;
        private final String birthDate;
        private final String email;

        public static GetUser from(UserOutput.GetUser output) {
            return GetUser.builder()
                    .userId(output.getUserId())
                    .userName(output.getUserName())
                    .genderCode(output.getGenderCode())
                    .birthDate(output.getBirthDate())
                    .email(output.getEmail())
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

        public static Join from(UserOutput.Join output) {
            return Join.builder()
                    .userId(output.getUserId())
                    .userName(output.getUserName())
                    .genderCode(output.getGenderCode())
                    .birthDate(output.getBirthDate())
                    .email(output.getEmail())
                    .build();
        }
    }

}
