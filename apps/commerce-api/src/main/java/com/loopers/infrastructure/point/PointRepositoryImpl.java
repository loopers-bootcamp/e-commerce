package com.loopers.infrastructure.point;

import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointHistory;
import com.loopers.domain.point.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PointRepositoryImpl implements PointRepository {

    private final PointJpaRepository pointJpaRepository;
    private final PointHistoryJpaRepository pointHistoryJpaRepository;

    @Override
    public Optional<Point> findOne(Long userId) {
        return pointJpaRepository.findByUserId(userId);
    }

    @Override
    public Optional<Point> findOneForUpdate(Long userId) {
        return pointJpaRepository.findByUserIdForUpdate(userId);
    }

    @Override
    public boolean existsPointByUserId(Long userId) {
        return pointJpaRepository.existsByUserId(userId);
    }

    @Override
    public List<PointHistory> findPointHistoriesByUserId(Long userId) {
        return pointHistoryJpaRepository.findPointHistoriesByUserId(userId);
    }

    @Override
    public Point savePoint(Point point) {
        return pointJpaRepository.save(point);
    }

    @Override
    public PointHistory savePointHistory(PointHistory history) {
        return pointHistoryJpaRepository.save(history);
    }

}
