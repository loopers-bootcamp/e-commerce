package com.loopers.interfaces.api.point;

import com.loopers.application.point.PointFacade;
import com.loopers.application.point.PointInput;
import com.loopers.application.point.PointOutput;
import com.loopers.interfaces.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/points")
public class PointV1Controller implements PointV1ApiSpec {

    private final PointFacade pointFacade;

    @GetMapping
    @Override
    public ApiResponse<PointResponse.GetPoint> getPoint(
            @RequestHeader("X-USER-ID")
            String userName
    ) {
        PointOutput.GetPoint output = pointFacade.getPoint(userName);
        PointResponse.GetPoint response = PointResponse.GetPoint.from(output);

        return ApiResponse.success(response);
    }

    @PostMapping("/charge")
    @Override
    public ApiResponse<PointResponse.Charge> charge(
            @RequestHeader("X-USER-ID")
            String userName,

            @Valid
            @RequestBody
            PointRequest.Charge request
    ) {
        PointInput.Charge input = PointInput.Charge.builder()
                .userName(userName)
                .amount(request.getAmount())
                .build();

        PointOutput.Charge output = pointFacade.charge(input);
        PointResponse.Charge response = PointResponse.Charge.from(output);

        return ApiResponse.success(response);
    }

}
