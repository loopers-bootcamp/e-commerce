package com.loopers.application.point;

import com.loopers.domain.point.PointCommand;
import com.loopers.domain.point.PointResult;
import com.loopers.domain.point.PointService;
import com.loopers.domain.user.UserResult;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PointFacade {

    private final PointService pointService;
    private final UserService userService;

    public PointOutput.GetPoint getPoint(String userName) {
        UserResult.GetUser userResult = userService.getUser(userName)
                .orElseThrow(() -> new BusinessException(CommonErrorType.NOT_FOUND));

        PointResult.GetPoint pointResult = pointService.getPoint(userResult.getUserId())
                .orElseThrow(() -> new BusinessException(CommonErrorType.NOT_FOUND));

        return PointOutput.GetPoint.from(pointResult);
    }

    public PointOutput.Charge charge(PointInput.Charge input) {
        UserResult.GetUser userResult = userService.getUser(input.getUserName())
                .orElseThrow(() -> new BusinessException(CommonErrorType.NOT_FOUND));

        PointCommand.Charge command = PointCommand.Charge.builder()
                .userId(userResult.getUserId())
                .amount(input.getAmount())
                .build();
        PointResult.Charge pointResult = pointService.charge(command);

        return PointOutput.Charge.from(pointResult);
    }

}
