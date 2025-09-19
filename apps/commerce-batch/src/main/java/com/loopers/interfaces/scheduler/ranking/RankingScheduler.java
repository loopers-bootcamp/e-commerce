package com.loopers.interfaces.scheduler.ranking;

import com.loopers.job.ranking.ProductRankingJobConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class RankingScheduler {

    private final JobLauncher jobLauncher;

    @Qualifier(ProductRankingJobConfig.JOB_NAME)
    private final Job job;

    @Scheduled(cron = "5 0 0 * * *", zone = "Asia/Seoul")
    public void launchRankingJob() throws Exception {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        JobParameters jobParameters = new JobParametersBuilder()
                .addLocalDate("date", yesterday)
                .toJobParameters();
        jobLauncher.run(job, jobParameters);
    }

}
