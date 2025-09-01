package com.loopers.interfaces.api.user;

import com.loopers.domain.user.UserCommand;
import com.loopers.domain.user.UserResult;
import com.loopers.domain.user.UserService;
import com.loopers.interfaces.api.ApiHeader;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserV1Controller implements UserV1ApiSpec {

    private final UserService userService;

    @GetMapping("/me")
    @Override
    public ApiResponse<UserResponse.GetUser> getUser(
            @RequestHeader(ApiHeader.USER_ID)
            String userName
    ) {
        UserResult.GetUser result = userService.getUser(userName)
                .orElseThrow(() -> new BusinessException(CommonErrorType.NOT_FOUND));
        UserResponse.GetUser response = UserResponse.GetUser.from(result);

        return ApiResponse.success(response);
    }

    @PostMapping
    @Override
    public ApiResponse<UserResponse.Join> join(
            @Valid
            @RequestBody
            UserRequest.Join request
    ) {
        UserCommand.Join command = UserCommand.Join.builder()
                .userName(request.getUserName())
                .gender(request.getGender())
                .birthDate(request.getBirthDate())
                .email(request.getEmail())
                .build();
        UserResult.Join result = userService.join(command);
        UserResponse.Join response = UserResponse.Join.from(result);

        return ApiResponse.success(response);
    }

}
