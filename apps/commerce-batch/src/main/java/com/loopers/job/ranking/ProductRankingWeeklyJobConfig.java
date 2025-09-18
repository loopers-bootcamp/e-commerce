package com.loopers.job.ranking;

import com.loopers.config.BatchJobProperties;
import com.loopers.domain.ranking.ProductRankingWeekly;
import com.loopers.support.CommonJobListener;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class ProductRankingWeeklyJobConfig {

    private static final String JOB_NAME = "product_ranking_weekly_job";
    private static final String STEP_NAME = "aggregate_step";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final CommonJobListener commonJobListener;
    private final BatchJobProperties batchJobProperties;

    @Bean(name = JOB_NAME)
    public Job productRankingWeeklyJob() {
        return new JobBuilder(JOB_NAME, jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(commonJobListener)
                .start(aggregateStep())
                .build();
    }

    @Bean(name = STEP_NAME)
    public Step aggregateStep() {
        int checkSize = batchJobProperties.getJobs().get(JOB_NAME)
                .getSteps().get(STEP_NAME)
                .getCheckSize();

        return new StepBuilder(STEP_NAME, jobRepository)
                .<ProductRankingWeekly, ProductRankingWeekly>chunk(checkSize, transactionManager)
//                .reader(reader)
//                .processor(processor)
//                .writer(writer)
                .build();
    }

}
