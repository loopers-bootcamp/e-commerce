package com.loopers.domain.ranking;

import com.loopers.annotation.ReadOnlyTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingService {

    private final RankingRepository rankingRepository;

    @ReadOnlyTransactional
    public RankingResult.SearchRanks searchRanks(RankingCommand.SearchRanks command) {
        Pageable pageable = PageRequest.of(command.page(), command.size());
        Page<RankingQueryResult.SearchRanks> page = rankingRepository.searchRanks(command.date(), pageable);

        return RankingResult.SearchRanks.from(page, pageable);
    }

}
