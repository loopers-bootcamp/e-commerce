package com.loopers.domain.point;

import java.util.List;
import java.util.Optional;

public interface PointRepository {

    Optional<Point> findOne(Long userId);

    Optional<Point> findOneForUpdate(Long userId);

    boolean existsPointByUserId(Long userId);

    List<PointHistory> findPointHistoriesByUserId(Long userId);

    Point savePoint(Point point);

    PointHistory savePointHistory(PointHistory point);

}
