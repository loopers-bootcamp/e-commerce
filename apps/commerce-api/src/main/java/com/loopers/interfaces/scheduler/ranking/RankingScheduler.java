package com.loopers.interfaces.scheduler.ranking;

import com.loopers.application.ranking.RankingFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class RankingScheduler {

    private final RankingFacade rankingFacade;

    @Scheduled(fixedDelayString = "PT1M", initialDelayString = "PT5S")
//    @Scheduled(cron = "0 30 23 * * *")
    public void createDailyRankings() {
//        LocalDate date = LocalDate.now().plusDays(1); // 30 mins early
        LocalDate date = LocalDate.now();

        log.info("Creating daily({}) rankings", date);
        int rankSize = rankingFacade.saveDailyRankings(date);
        log.info("Created daily({}) rankings: {} products", date, rankSize);
    }

}
