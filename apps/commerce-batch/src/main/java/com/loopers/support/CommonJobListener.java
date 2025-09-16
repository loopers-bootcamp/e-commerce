package com.loopers.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CommonJobListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        String jobName = jobExecution.getJobInstance().getJobName();
        Long jobId = jobExecution.getJobId();

        log.info("Job 시작={}, Job ID={}", jobName, jobId);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        String jobName = jobExecution.getJobInstance().getJobName();
        String status = jobExecution.getStatus().toString();
        long jobId = jobExecution.getJobId();

        log.info("Job 완료={}, Job ID={}, Status={}", jobName, jobId, status);

        if (jobExecution.getStatus().isUnsuccessful()) {
            String exitCode = jobExecution.getExitStatus().getExitCode();
            log.error("Job 실패={}, Job ID={}, Exit Code={}", jobName, jobId, exitCode);
        }
    }

}
