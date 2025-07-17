package com.loopers.interfaces.api.user;

import com.loopers.application.user.UserFacade;
import com.loopers.application.user.UserInput;
import com.loopers.application.user.UserOutput;
import com.loopers.interfaces.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserV1Controller implements UserV1ApiSpec {

    private final UserFacade userFacade;

    @GetMapping("/me")
    @Override
    public ApiResponse<UserResponse.GetUser> getUser(
            @RequestHeader("X-USER-ID")
            String userName
    ) {
        UserOutput.GetUser output = userFacade.getUser(userName);
        UserResponse.GetUser response = UserResponse.GetUser.from(output);

        return ApiResponse.success(response);
    }

    @PostMapping
    @Override
    public ApiResponse<UserResponse.JoinUser> joinUser(
            @Valid
            @RequestBody
            UserRequest.JoinUser request
    ) {
        UserInput.Join input = request.toInput();
        UserOutput.Join output = userFacade.join(input);
        UserResponse.JoinUser response = UserResponse.JoinUser.from(output);

        return ApiResponse.success(response);
    }

}
