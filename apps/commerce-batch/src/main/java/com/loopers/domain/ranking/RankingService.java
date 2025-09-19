package com.loopers.domain.ranking;

import com.loopers.annotation.ReadOnlyTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final RankingRepository rankingRepository;

    @ReadOnlyTransactional
    public RankingResult.GetDaily getDaily(RankingCommand.GetDaily command) {
        List<ProductRankingDaily> rankings = rankingRepository.findDailyRanking(command.startDate(), command.endDate());
        return RankingResult.GetDaily.from(rankings);
    }

    @Transactional
    public void aggregateDaily(RankingCommand.AggregateDaily command) {
        List<Map.Entry<Long, Double>> entries = command.entries();
        if (CollectionUtils.isEmpty(entries)) {
            return;
        }

        for (int i = 0; i < entries.size(); i++) {
            ProductRankingDaily ranking = ProductRankingDaily.builder()
                    .date(command.date())
                    .productId(entries.get(i).getKey())
                    .rank(i + 1)
                    .score(entries.get(i).getValue())
                    .build();

            rankingRepository.merge(ranking);
        }
    }

    @Transactional
    public void aggregateWeekly(RankingCommand.AggregateWeekly command) {
        List<Long> productIds = command.productIds();
        if (CollectionUtils.isEmpty(productIds)) {
            return;
        }

        for (int i = 0; i < productIds.size(); i++) {
            ProductRankingWeekly ranking = ProductRankingWeekly.builder()
                    .standardDate(command.date())
                    .productId(productIds.get(i))
                    .rank(i + 1)
                    .build();

            rankingRepository.merge(ranking);
        }
    }

    @Transactional
    public void aggregateMonthly(RankingCommand.AggregateMonthly command) {
        List<Long> productIds = command.productIds();
        if (CollectionUtils.isEmpty(productIds)) {
            return;
        }

        for (int i = 0; i < productIds.size(); i++) {
            ProductRankingMonthly ranking = ProductRankingMonthly.builder()
                    .standardDate(command.date())
                    .productId(productIds.get(i))
                    .rank(i + 1)
                    .build();

            rankingRepository.merge(ranking);
        }
    }

}
