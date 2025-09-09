package com.loopers.domain.ranking;

import com.loopers.annotation.ReadOnlyTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final RankingCacheRepository rankingCacheRepository;

    @ReadOnlyTransactional
    public RankingResult.FindRanks findRanks(LocalDate date) {
        List<RankingQueryResult.FindRanks> ranks = rankingCacheRepository.findRanks(date);
        return RankingResult.FindRanks.from(ranks);
    }

    @ReadOnlyTransactional
    public void searchRankings(RankingCommand.SearchRankings command) {
        rankingCacheRepository.searchRankings(
                command.getDate(),
                command.getPage(),
                command.getSize()
        );
    }

}
