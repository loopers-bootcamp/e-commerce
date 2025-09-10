package com.loopers.domain.ranking;

import com.loopers.annotation.ReadOnlyTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
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
    public RankingResult.SearchRankings searchRankings(RankingCommand.SearchRankings command) {
        Pageable pageable = PageRequest.of(command.getPage(), command.getSize());
        Page<RankingQueryResult.SearchRankings> page = rankingCacheRepository.searchRankings(command.getDate(), pageable);

        return RankingResult.SearchRankings.from(page, pageable);
    }

    public void saveRankings(RankingCommand.SaveRankings command) {
        List<RankingQueryCommand.SaveRankings> queryCommand = RankingQueryCommand.SaveRankings.from(command);
        rankingCacheRepository.saveRankings(command.getDate(), queryCommand);
    }

}
