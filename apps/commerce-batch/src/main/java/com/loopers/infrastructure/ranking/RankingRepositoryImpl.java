package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.ProductRankingDaily;
import com.loopers.domain.ranking.ProductRankingMonthly;
import com.loopers.domain.ranking.ProductRankingWeekly;
import com.loopers.domain.ranking.RankingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class RankingRepositoryImpl implements RankingRepository {

    private final ProductRankingDailyJpaRepository productRankingDailyJpaRepository;
    private final ProductRankingWeeklyJpaRepository productRankingWeeklyJpaRepository;
    private final ProductRankingMonthlyJpaRepository productRankingMonthlyJpaRepository;

    @Override
    public List<ProductRankingDaily> findDailyRanking(LocalDate startDate, LocalDate endDate) {
        return productRankingDailyJpaRepository.findByDateBetween(startDate, endDate);
    }

    @Override
    public boolean merge(ProductRankingDaily ranking) {
        ranking.prePersist();
        return productRankingDailyJpaRepository.merge(
                ranking.getDate(),
                ranking.getProductId(),
                ranking.getRank(),
                ranking.getCreatedAt(),
                ranking.getUpdatedAt()
        ) == 1;
    }

    @Override
    public boolean merge(ProductRankingWeekly ranking) {
        ranking.prePersist();
        return productRankingWeeklyJpaRepository.merge(
                ranking.getYearWeek(),
                ranking.getProductId(),
                ranking.getRank(),
                ranking.getCreatedAt(),
                ranking.getUpdatedAt()
        ) == 1;
    }

    @Override
    public boolean merge(ProductRankingMonthly ranking) {
        ranking.prePersist();
        return productRankingMonthlyJpaRepository.merge(
                ranking.getYearMonth(),
                ranking.getProductId(),
                ranking.getRank(),
                ranking.getCreatedAt(),
                ranking.getUpdatedAt()
        ) == 1;
    }

}
