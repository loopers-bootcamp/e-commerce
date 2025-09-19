package com.loopers.domain.ranking;

import com.loopers.annotation.ReadOnlyTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingService {

    private final RankingRepository rankingRepository;

    @ReadOnlyTransactional
    public Optional<Long> findRank(RankingCommand.FindRank command) {
        return rankingRepository.findRank(command.date(), command.productId());
    }

    @ReadOnlyTransactional
    public RankingResult.SearchDaily searchDaily(RankingCommand.SearchDaily command) {
        Pageable pageable = PageRequest.of(command.page(), command.size());
        Page<RankingQueryResult.SearchRanks> page = rankingRepository.searchRanks(command.date(), pageable);

        return RankingResult.SearchDaily.from(page, pageable);
    }

    @ReadOnlyTransactional
    public RankingResult.SearchWeekly searchWeekly(RankingCommand.SearchWeekly command) {
        Pageable pageable = PageRequest.of(command.page(), command.size());
        Page<RankingQueryResult.SearchRanks> page = rankingRepository.searchRanks(command.yearWeek(), pageable);

        return RankingResult.SearchWeekly.from(page, pageable);
    }

    @ReadOnlyTransactional
    public RankingResult.SearchMonthly searchMonthly(RankingCommand.SearchMonthly command) {
        Pageable pageable = PageRequest.of(command.page(), command.size());
        Page<RankingQueryResult.SearchRanks> page = rankingRepository.searchRanks(command.yearMonth(), pageable);

        return RankingResult.SearchMonthly.from(page, pageable);
    }

}
