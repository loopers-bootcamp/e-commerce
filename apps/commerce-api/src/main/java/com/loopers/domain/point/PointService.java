package com.loopers.domain.point;

import com.loopers.annotation.ReadOnlyTransactional;
import com.loopers.domain.point.attribute.Cause;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointRepository pointRepository;

    @ReadOnlyTransactional
    public Optional<PointResult.GetPoint> getPoint(Long userId) {
        return pointRepository.findOne(userId)
                .map(PointResult.GetPoint::from);
    }

    @ReadOnlyTransactional
    public List<PointHistory> getPointHistories(Long userId) {
        return pointRepository.findPointHistoriesByUserId(userId);
    }

    @Transactional
    public PointResult.Create create(Long userId) {
        if (pointRepository.existsPointByUserId(userId)) {
            throw new BusinessException(CommonErrorType.CONFLICT);
        }

        Point point = Point.builder()
                .userId(userId)
                .balance(0L)
                .build();

        pointRepository.savePoint(point);

        return PointResult.Create.from(point);
    }

    @Transactional
    public PointResult.Charge charge(PointCommand.Charge command) {
        Long userId = command.getUserId();
        Long amount = command.getAmount();

        Point point = pointRepository.findOneForUpdate(userId)
                .orElseThrow(() -> new BusinessException(CommonErrorType.NOT_FOUND));

        point.charge(amount);
        pointRepository.savePoint(point);

        PointHistory history = PointHistory.builder()
                .userId(userId)
                .cause(Cause.CHARGE)
                .amount(amount)
                .build();
        pointRepository.savePointHistory(history);

        return PointResult.Charge.from(point);
    }

    @Transactional
    public PointResult.Spend spend(PointCommand.Spend command) {
        Long userId = command.getUserId();
        Long amount = command.getAmount();

        Point point = pointRepository.findOneForUpdate(userId)
                .orElseThrow(() -> new BusinessException(CommonErrorType.NOT_FOUND));

        point.spend(amount);
        pointRepository.savePoint(point);

        PointHistory history = PointHistory.builder()
                .userId(userId)
                .cause(Cause.PURCHASE)
                .amount(amount)
                .build();
        pointRepository.savePointHistory(history);

        return PointResult.Spend.from(point);
    }

}
