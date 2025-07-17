package com.loopers.interfaces.api.user;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "User V1 API", description = "사용자 API V1")
public interface UserV1ApiSpec {

    @Operation(
            summary = "내 정보 조회",
            description = "아이디로 회원 정보를 조회합니다."
    )
    ApiResponse<UserResponse.GetUser> getUser(
            @Schema(name = "X-USER-ID", description = "조회할 사용자의 ID")
            String userName
    );

    @Operation(
            summary = "회원 가입",
            description = "새로운 회원을 등록합니다."
    )
    ApiResponse<UserResponse.JoinUser> joinUser(
            @RequestBody(description = "회원 정보")
            UserRequest.JoinUser request
    );

}
