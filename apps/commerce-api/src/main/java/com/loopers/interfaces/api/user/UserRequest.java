package com.loopers.interfaces.api.user;

import com.loopers.application.user.UserInput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserRequest {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Join {
        @NotBlank
        private final String userName;
        @NotNull
        private final Integer genderCode;
        @NotBlank
        private final String birthDate;
        @NotBlank
        private final String email;

        public UserInput.Join toInput() {
            return UserInput.Join.builder()
                    .userName(this.userName)
                    .genderCode(this.genderCode)
                    .birthDate(this.birthDate)
                    .email(this.email)
                    .build();
        }
    }

}
