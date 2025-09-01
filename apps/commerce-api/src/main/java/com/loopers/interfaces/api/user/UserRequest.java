package com.loopers.interfaces.api.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.loopers.domain.user.attribute.Email;
import com.loopers.domain.user.attribute.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserRequest {

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Join {
        @NotBlank
        private final String userName;
        @NotNull
        private final Gender gender;
        @NotNull
        @PastOrPresent
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private final LocalDate birthDate;
        @NotNull
        private final Email email;
    }

}
