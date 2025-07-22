package com.loopers.domain.user;

import com.loopers.domain.user.attribute.Email;
import com.loopers.domain.user.attribute.Gender;
import lombok.*;

import java.time.LocalDate;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserResult {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class GetUser {
        private final Long userId;
        private final String userName;
        private final Gender gender;
        private final LocalDate birthDate;
        private final Email email;

        public static GetUser from(User user) {
            return GetUser.builder()
                    .userId(user.getId())
                    .userName(user.getName())
                    .gender(user.getGender())
                    .birthDate(user.getBirthDate())
                    .email(user.getEmail())
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

        public static Join from(User user) {
            return Join.builder()
                    .userId(user.getId())
                    .userName(user.getName())
                    .gender(user.getGender())
                    .birthDate(user.getBirthDate())
                    .email(user.getEmail())
                    .build();
        }
    }

}
