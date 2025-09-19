package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.ProductRankingMonthly;
import com.loopers.domain.ranking.ProductRankingWeekly;
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
import org.threeten.extra.YearWeek;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class RankingRepositoryImpl implements RankingRepository {

    private final ProductRankingWeeklyJpaRepository productRankingWeeklyJpaRepository;
    private final ProductRankingMonthlyJpaRepository productRankingMonthlyJpaRepository;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public Optional<Long> findRank(LocalDate date, Long productId) {
        String day = date.format(DateTimeFormatter.BASIC_ISO_DATE);
        String key = "metric.product.all:" + day;
        String member = StringUtils.invert9sComplement(productId.toString());

        ZSetOperations<String, String> zSet = stringRedisTemplate.opsForZSet();
        Long rank = zSet.reverseRank(key, member);

        return Optional.ofNullable(rank).map(i -> i + 1L);
    }

    @Override
    public Page<RankingQueryResult.SearchRanks> searchRanks(LocalDate date, Pageable pageable) {
        String day = date.format(DateTimeFormatter.BASIC_ISO_DATE);
        String key = "metric.product.all:" + day;

        // page 1번을 offset 0으로 변환한다.
        Pageable pageRequest = pageable.withPage(pageable.getPageNumber() - 1);
        long start = pageRequest.getOffset();
        long end = pageRequest.getOffset() + pageRequest.getPageSize();

        ZSetOperations<String, String> zSet = stringRedisTemplate.opsForZSet();
        Set<String> members = zSet.reverseRange(key, start, end);

        if (CollectionUtils.isEmpty(members)) {
            return Page.empty(pageRequest);
        }

        List<Long> productIds = members.stream()
                .map(StringUtils::invert9sComplement)
                .map(Long::parseLong)
                .toList();

        List<RankingQueryResult.SearchRanks> content = new ArrayList<>();
        for (int i = 0; i < productIds.size(); i++) {
            content.add(new RankingQueryResult.SearchRanks(productIds.get(i), i + 1L));
        }

        return PageableExecutionUtils.getPage(List.copyOf(content), pageRequest,
                () -> Objects.requireNonNullElse(zSet.zCard(key), 0L));
    }

    @Override
    public Page<RankingQueryResult.SearchRanks> searchRanks(YearWeek yearWeek, Pageable pageable) {
        Page<ProductRankingWeekly> page = productRankingWeeklyJpaRepository.findByYearWeek(yearWeek, pageable);

        List<RankingQueryResult.SearchRanks> content = page.getContent()
                .stream()
                .map(item -> new RankingQueryResult.SearchRanks(
                        item.getProductId(),
                        Long.valueOf(item.getRank())
                ))
                .toList();

        return PageableExecutionUtils.getPage(content, pageable, page::getTotalElements);
    }

    @Override
    public Page<RankingQueryResult.SearchRanks> searchRanks(YearMonth yearMonth, Pageable pageable) {
        Page<ProductRankingMonthly> page = productRankingMonthlyJpaRepository.findByYearMonth(yearMonth, pageable);

        List<RankingQueryResult.SearchRanks> content = page.getContent()
                .stream()
                .map(item -> new RankingQueryResult.SearchRanks(
                        item.getProductId(),
                        Long.valueOf(item.getRank())
                ))
                .toList();

        return PageableExecutionUtils.getPage(content, pageable, page::getTotalElements);
    }

}
