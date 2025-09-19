package com.loopers.job.ranking;

import com.loopers.domain.ranking.RankingCommand;
import com.loopers.domain.ranking.RankingResult;
import com.loopers.domain.ranking.RankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingDouble;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class MonthlyRankingAggregationTasklet implements Tasklet {

    private final RankingService rankingService;

    @Value("#{jobParameters['date'] ?: null}")
    private LocalDate date;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        // 지난 달 1일 ~ 말일
        LocalDate startDate = date.with(TemporalAdjusters.firstDayOfMonth()).minusMonths(1);
        LocalDate endDate = date.minusDays(1);

        RankingResult.GetDaily ranking = rankingService.getDaily(new RankingCommand.GetDaily(startDate, endDate));

        if (CollectionUtils.isEmpty(ranking.items())) {
            log.info("No monthly ranking data found");
            return RepeatStatus.FINISHED;
        }

        Map<Long, Double> productScores = ranking.items()
                .stream()
                .collect(groupingBy(
                        RankingResult.GetDaily.Item::productId,
                        summingDouble(RankingResult.GetDaily.Item::score)
                ));

        List<Long> productIds = productScores.entrySet()
                .stream()
                .sorted(Comparator
                        .<Map.Entry<Long, Double>>comparingDouble(Map.Entry::getValue).reversed()
                        .thenComparing(Map.Entry::getKey))
                .map(Map.Entry::getKey)
                .toList();

        // Top 100
        rankingService.aggregateMonthly(new RankingCommand.AggregateMonthly(date, productIds));

        log.info("Monthly ranking aggregation completed: {}", productIds.size());

        return RepeatStatus.FINISHED;
    }

}
