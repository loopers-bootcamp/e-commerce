package com.loopers.job.ranking;

import com.loopers.domain.ranking.RankingCommand;
import com.loopers.domain.ranking.RankingService;
import com.loopers.support.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class DailyRankingAggregationTasklet implements Tasklet {

    private final StringRedisTemplate stringRedisTemplate;
    private final RankingService rankingService;

    @Value("#{jobParameters['date'] ?: null}")
    private LocalDate date;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        String day = date.format(DateTimeFormatter.BASIC_ISO_DATE);
        String all = "metric.product.all:" + day;

        // Top 100
        ZSetOperations<String, String> zSet = stringRedisTemplate.opsForZSet();
        Set<ZSetOperations.TypedTuple<String>> tuples = zSet.reverseRangeWithScores(all, 0, 99);

        if (CollectionUtils.isEmpty(tuples)) {
            log.info("No daily ranking data found");
            return RepeatStatus.FINISHED;
        }

        List<Map.Entry<Long, Double>> entries = tuples.stream()
                .map(tuple -> Map.entry(
                        Long.parseLong(StringUtils.invert9sComplement(tuple.getValue())),
                        tuple.getScore()
                ))
                .toList();
        rankingService.aggregateDaily(new RankingCommand.AggregateDaily(date, entries));

        log.info("Daily ranking aggregation completed: {}", entries.size());

        return RepeatStatus.FINISHED;
    }

}
