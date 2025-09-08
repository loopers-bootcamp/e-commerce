package com.loopers.config.ranking;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "ranking.weight")
public class WeightedRankingProperties {

    private final double view;
    private final double like;
    private final double sale;

}
