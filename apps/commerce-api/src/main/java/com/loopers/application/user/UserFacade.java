package com.loopers.application.user;

import com.loopers.domain.point.PointService;
import com.loopers.domain.user.UserResult;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UserFacade {

    private final UserService userService;
    private final PointService pointService;

    public UserOutput.GetUser getUser(String userName) {
        UserResult.GetUser result = userService.getUser(userName)
                .orElseThrow(() -> new BusinessException(CommonErrorType.NOT_FOUND));

        return UserOutput.GetUser.from(result);
    }

    @Transactional
    public UserOutput.Join join(UserInput.Join input) {
        UserResult.Join result = userService.join(input.toCommand());
        pointService.create(result.getUserId());

        return UserOutput.Join.from(result);
    }

}
