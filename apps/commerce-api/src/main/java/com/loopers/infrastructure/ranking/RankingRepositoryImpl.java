package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.RankingQueryResult;
import com.loopers.domain.ranking.RankingRepository;
import com.loopers.support.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class RankingRepositoryImpl implements RankingRepository {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public Page<RankingQueryResult.SearchRanks> searchRanks(LocalDate date, Pageable pageable) {
        String day = date.format(DateTimeFormatter.BASIC_ISO_DATE);
        String all = "metric.product.all:" + day;

        // Top 1000
        ZSetOperations<String, String> zSet = stringRedisTemplate.opsForZSet();

        // page 1번을 offset 0으로 변환한다.
        Pageable pageRequest = pageable.withPage(pageable.getPageNumber() - 1);
        long start = pageRequest.getOffset();
        long end = pageRequest.getOffset() + pageRequest.getPageSize();

        Set<String> members = zSet.reverseRange(all, start, end);

        if (CollectionUtils.isEmpty(members)) {
            return Page.empty(pageRequest);
        }

        List<Long> productIds = members.stream()
                .map(StringUtils::invert9sComplement)
                .map(Long::parseLong)
                .toList();

        List<RankingQueryResult.SearchRanks> content = new ArrayList<>();
        for (int i = 0; i < productIds.size(); i++) {
            content.add(new RankingQueryResult.SearchRanks(productIds.get(i), i + 1));
        }

        return PageableExecutionUtils.getPage(List.copyOf(content), pageRequest, () -> zSet.zCard(all));
    }

}
