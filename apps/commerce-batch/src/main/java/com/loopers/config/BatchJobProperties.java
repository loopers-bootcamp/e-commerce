package com.loopers.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.util.Map;

@Getter
@ConfigurationProperties(prefix = "batch")
public class BatchJobProperties {

    private final Map<String, Job> jobs;

    @ConstructorBinding
    public BatchJobProperties(Map<String, Job> jobs) {
        this.jobs = jobs;
    }

    @Getter
    @RequiredArgsConstructor
    public static class Job {
        private final Map<String, Step> steps;
    }

    @Getter
    @RequiredArgsConstructor
    public static class Step {
        private final int checkSize;
    }

}
