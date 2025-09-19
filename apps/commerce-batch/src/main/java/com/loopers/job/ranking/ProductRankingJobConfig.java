package com.loopers.job.ranking;

import com.loopers.support.CommonJobListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Configuration
@RequiredArgsConstructor
public class ProductRankingJobConfig {

    public static final String JOB_NAME = "product_ranking_job";
    private static final String DAILY_RANKING_STEP_NAME = "daily_ranking_step";
    private static final String WEEKLY_RANKING_STEP_NAME = "weekly_ranking_step";
    private static final String MONTHLY_RANKING_STEP_NAME = "monthly_ranking_step";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final CommonJobListener commonJobListener;
    @Qualifier(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME)
    private final TaskExecutor taskExecutor;

    private final DailyRankingAggregationTasklet dailyRankingAggregationTasklet;
    private final WeeklyRankingAggregationTasklet weeklyRankingAggregationTasklet;
    private final MonthlyRankingAggregationTasklet monthlyRankingAggregationTasklet;

    @Bean(name = JOB_NAME)
    Job productRankingJob() {
        return new JobBuilder(JOB_NAME, jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(commonJobListener)
                .flow(dailyRankingStep())
                // daily 후 weekly/monthly를 병렬로 실행
                .next(new FlowBuilder<Flow>("parallelAfterDailyRanking")
                        .split(taskExecutor)
                        .add(weeklyRankingFlow(), monthlyRankingFlow())
                        .build()
                )
                .end()
                .build();
    }

    @Bean(name = DAILY_RANKING_STEP_NAME)
    Step dailyRankingStep() {
        return new StepBuilder(DAILY_RANKING_STEP_NAME, jobRepository)
                .tasklet(dailyRankingAggregationTasklet, transactionManager)
                .build();
    }

    @Bean(name = WEEKLY_RANKING_STEP_NAME)
    Step weeklyRankingStep() {
        return new StepBuilder(WEEKLY_RANKING_STEP_NAME, jobRepository)
                .tasklet(weeklyRankingAggregationTasklet, transactionManager)
                .build();
    }

    @Bean(name = MONTHLY_RANKING_STEP_NAME)
    Step monthlyRankingStep() {
        return new StepBuilder(MONTHLY_RANKING_STEP_NAME, jobRepository)
                .tasklet(monthlyRankingAggregationTasklet, transactionManager)
                .build();
    }

    @Bean
    Flow weeklyRankingFlow() {
        return new FlowBuilder<Flow>("weeklyRankingFlow")
                .start(weeklyDecider())
                .on("RUN").to(weeklyRankingStep())
                .from(weeklyDecider())
                .on("SKIP").end()
                .build();
    }

    @Bean
    Flow monthlyRankingFlow() {
        return new FlowBuilder<Flow>("monthlyRankingFlow")
                .start(monthlyDecider())
                .on("RUN").to(monthlyRankingStep())
                .from(monthlyDecider())
                .on("SKIP").end()
                .build();
    }

    @Bean
    WeeklyDecider weeklyDecider() {
        return new WeeklyDecider();
    }

    @Bean
    MonthlyDecider monthlyDecider() {
        return new MonthlyDecider();
    }

    // -------------------------------------------------------------------------------------------------

    @Slf4j
    public static class WeeklyDecider implements JobExecutionDecider {
        @Override
        public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
            LocalDate date = jobExecution.getJobParameters().getLocalDate("date");
            String status = date.getDayOfWeek() == DayOfWeek.MONDAY ? "RUN" : "SKIP";

            if (status.equals("SKIP")) {
                log.debug("Skip weekly ranking aggregation");
            }

            return new FlowExecutionStatus(status);
        }
    }

    @Slf4j
    public static class MonthlyDecider implements JobExecutionDecider {
        @Override
        public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
            LocalDate date = jobExecution.getJobParameters().getLocalDate("date");
            String status = date.getDayOfMonth() == 1 ? "RUN" : "SKIP";

            if (status.equals("SKIP")) {
                log.debug("Skip monthly ranking aggregation");
            }

            return new FlowExecutionStatus(status);
        }
    }

}
