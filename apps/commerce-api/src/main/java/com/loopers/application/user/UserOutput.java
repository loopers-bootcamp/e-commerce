package com.loopers.application.user;

import com.loopers.domain.user.UserResult;
import com.loopers.domain.user.attribute.Email;
import com.loopers.domain.user.attribute.Gender;
import lombok.*;

import java.time.LocalDate;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserOutput {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class GetUser {
        private final Long userId;
        private final String userName;
        private final Gender gender;
        private final LocalDate birthDate;
        private final Email email;

        public static GetUser from(UserResult.GetUser result) {
            return GetUser.builder()
                    .userId(result.getUserId())
                    .userName(result.getUserName())
                    .gender(result.getGender())
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
        private final Gender gender;
        private final LocalDate birthDate;
        private final Email email;

        public static Join from(UserResult.Join result) {
            return Join.builder()
                    .userId(result.getUserId())
                    .userName(result.getUserName())
                    .gender(result.getGender())
                    .birthDate(result.getBirthDate())
                    .email(result.getEmail())
                    .build();
        }
    }

}
