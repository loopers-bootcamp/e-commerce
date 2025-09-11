package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.RankingQueryResult;
import com.loopers.support.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests focus on the diff for RankingRepositoryImpl.
 *
 * Testing library/framework used:
 * - JUnit 5 (Jupiter)
 * - Mockito (MockitoExtension)
 */
@ExtendWith(MockitoExtension.class)
class RankingRepositoryImplTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ZSetOperations<String, String> zSet;

    @InjectMocks
    private RankingRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        when(stringRedisTemplate.opsForZSet()).thenReturn(zSet);
    }

    @Nested
    class FindRank {

        @Test
        @DisplayName("findRank returns Optional.of(rank) when member rank exists")
        void findRank_returnsRank_whenPresent() {
            // given
            LocalDate date = LocalDate.of(2025, 1, 15);
            Long productId = 42L;
            String day = date.format(DateTimeFormatter.BASIC_ISO_DATE);
            String expectedKey = "metric.product.all:" + day;
            String expectedMember = StringUtils.invert9sComplement(productId.toString());

            when(zSet.rank(expectedKey, expectedMember)).thenReturn(123L);

            // when
            Optional<Long> result = repository.findRank(date, productId);

            // then
            assertTrue(result.isPresent());
            assertEquals(123L, result.get());
            verify(zSet).rank(expectedKey, expectedMember);
            verifyNoMoreInteractions(zSet);
        }

        @Test
        @DisplayName("findRank returns Optional.empty() when rank is null")
        void findRank_returnsEmpty_whenNull() {
            // given
            LocalDate date = LocalDate.of(2025, 1, 15);
            Long productId = 7L;
            String day = date.format(DateTimeFormatter.BASIC_ISO_DATE);
            String expectedKey = "metric.product.all:" + day;
            String expectedMember = StringUtils.invert9sComplement(productId.toString());

            when(zSet.rank(expectedKey, expectedMember)).thenReturn(null);

            // when
            Optional<Long> result = repository.findRank(date, productId);

            // then
            assertTrue(result.isEmpty());
            verify(zSet).rank(expectedKey, expectedMember);
            verifyNoMoreInteractions(zSet);
        }
    }

    @Nested
    class SearchRanks {

        @Test
        @DisplayName("searchRanks returns empty Page when Redis returns empty members")
        void searchRanks_emptyWhenNoMembers() {
            // given
            LocalDate date = LocalDate.of(2025, 2, 1);
            PageRequest pageableInput = PageRequest.of(1, 3); // input expected 1-based
            String day = date.format(DateTimeFormatter.BASIC_ISO_DATE);
            String expectedKey = "metric.product.all:" + day;

            when(zSet.reverseRange(eq(expectedKey), anyLong(), anyLong())).thenReturn(Collections.emptySet());

            // when
            Page<RankingQueryResult.SearchRanks> page = repository.searchRanks(date, pageableInput);

            // then
            assertTrue(page.getContent().isEmpty());
            assertEquals(0L, page.getTotalElements());

            // zCard should not be called when members are empty
            verify(zSet, never()).zCard(anyString());

            // Verify paging math (start=0, end=3)
            ArgumentCaptor<Long> startCap = ArgumentCaptor.forClass(Long.class);
            ArgumentCaptor<Long> endCap = ArgumentCaptor.forClass(Long.class);
            verify(zSet).reverseRange(eq(expectedKey), startCap.capture(), endCap.capture());
            assertEquals(0L, startCap.getValue());
            assertEquals(3L, endCap.getValue());

            verifyNoMoreInteractions(zSet);
        }

        @Test
        @DisplayName("searchRanks returns empty Page when Redis returns null members")
        void searchRanks_emptyWhenNullMembers() {
            // given
            LocalDate date = LocalDate.of(2025, 2, 2);
            PageRequest pageableInput = PageRequest.of(1, 2);
            String day = date.format(DateTimeFormatter.BASIC_ISO_DATE);
            String expectedKey = "metric.product.all:" + day;

            when(zSet.reverseRange(eq(expectedKey), anyLong(), anyLong())).thenReturn(null);

            // when
            Page<RankingQueryResult.SearchRanks> page = repository.searchRanks(date, pageableInput);

            // then
            assertTrue(page.getContent().isEmpty());
            assertEquals(0L, page.getTotalElements());
            verify(zSet, never()).zCard(anyString());
            verify(zSet).reverseRange(eq(expectedKey), anyLong(), anyLong());
            verifyNoMoreInteractions(zSet);
        }

        @Test
        @DisplayName("searchRanks returns mapped content with ranks starting from 1 and preserves order")
        void searchRanks_happyPath_firstPage() {
            // given
            LocalDate date = LocalDate.of(2025, 2, 1);
            PageRequest pageableInput = PageRequest.of(1, 3);

            String day = date.format(DateTimeFormatter.BASIC_ISO_DATE);
            String expectedKey = "metric.product.all:" + day;

            List<Long> originalIds = List.of(101L, 202L, 303L);
            LinkedHashSet<String> encoded = new LinkedHashSet<>();
            for (Long id : originalIds) {
                encoded.add(StringUtils.invert9sComplement(id.toString()));
            }

            when(zSet.reverseRange(expectedKey, 0L, 3L)).thenReturn(encoded);
            when(zSet.zCard(expectedKey)).thenReturn(100L);

            // when
            Page<RankingQueryResult.SearchRanks> page = repository.searchRanks(date, pageableInput);

            // then
            assertEquals(100L, page.getTotalElements());
            assertEquals(3, page.getContent().size());

            List<Long> ids = page.getContent().stream().map(RankingQueryResult.SearchRanks::productId).toList();
            List<Long> ranks = page.getContent().stream().map(RankingQueryResult.SearchRanks::rank).toList();

            assertIterableEquals(originalIds, ids);
            assertIterableEquals(List.of(1L, 2L, 3L), ranks);

            verify(zSet).reverseRange(expectedKey, 0L, 3L);
            verify(zSet).zCard(expectedKey);
            verifyNoMoreInteractions(zSet);
        }

        @Test
        @DisplayName("searchRanks computes start/end using adjusted page (2nd page -> start=3, end=6)")
        void searchRanks_pagingMath_secondPage() {
            // given
            LocalDate date = LocalDate.of(2025, 2, 1);
            PageRequest pageableInput = PageRequest.of(2, 3); // withPage(1) -> offset=3; end=6

            String day = date.format(DateTimeFormatter.BASIC_ISO_DATE);
            String expectedKey = "metric.product.all:" + day;

            LinkedHashSet<String> encoded = new LinkedHashSet<>();
            List<Long> originalIds = List.of(404L, 505L, 606L);
            for (Long id : originalIds) {
                encoded.add(StringUtils.invert9sComplement(id.toString()));
            }

            when(zSet.reverseRange(eq(expectedKey), anyLong(), anyLong())).thenReturn(encoded);
            when(zSet.zCard(expectedKey)).thenReturn(12L);

            // when
            Page<RankingQueryResult.SearchRanks> page = repository.searchRanks(date, pageableInput);

            // then
            ArgumentCaptor<Long> startCap = ArgumentCaptor.forClass(Long.class);
            ArgumentCaptor<Long> endCap = ArgumentCaptor.forClass(Long.class);
            verify(zSet).reverseRange(eq(expectedKey), startCap.capture(), endCap.capture());

            assertEquals(3L, startCap.getValue());
            assertEquals(6L, endCap.getValue());

            assertEquals(3, page.getContent().size());
            assertIterableEquals(originalIds,
                page.getContent().stream().map(RankingQueryResult.SearchRanks::productId).toList());
            assertIterableEquals(List.of(1L, 2L, 3L),
                page.getContent().stream().map(RankingQueryResult.SearchRanks::rank).toList());

            assertEquals(12L, page.getTotalElements());
            verify(zSet).zCard(expectedKey);
            verifyNoMoreInteractions(zSet);
        }

        @Test
        @DisplayName("searchRanks sets totalElements to 0 when zCard returns null")
        void searchRanks_totalElementsZeroWhenZCardNull() {
            // given
            LocalDate date = LocalDate.of(2025, 2, 1);
            PageRequest pageableInput = PageRequest.of(1, 2);

            String day = date.format(DateTimeFormatter.BASIC_ISO_DATE);
            String expectedKey = "metric.product.all:" + day;

            LinkedHashSet<String> encoded = new LinkedHashSet<>();
            encoded.add(StringUtils.invert9sComplement("11"));
            encoded.add(StringUtils.invert9sComplement("22"));

            when(zSet.reverseRange(expectedKey, 0L, 2L)).thenReturn(encoded);
            when(zSet.zCard(expectedKey)).thenReturn(null);

            // when
            Page<RankingQueryResult.SearchRanks> page = repository.searchRanks(date, pageableInput);

            // then
            assertEquals(0L, page.getTotalElements());
            assertEquals(2, page.getContent().size());
            assertEquals(11L, page.getContent().get(0).productId());
            assertEquals(22L, page.getContent().get(1).productId());
            assertEquals(1L, page.getContent().get(0).rank());
            assertEquals(2L, page.getContent().get(1).rank());

            verify(zSet).reverseRange(expectedKey, 0L, 2L);
            verify(zSet).zCard(expectedKey);
            verifyNoMoreInteractions(zSet);
        }

        @Test
        @DisplayName("searchRanks throws when pageable is 0-based (withPage(-1) -> IllegalArgumentException)")
        void searchRanks_throwsWhenZeroBasedPageProvided() {
            // given
            LocalDate date = LocalDate.of(2025, 2, 3);

            // when/then
            assertThrows(IllegalArgumentException.class,
                () -> repository.searchRanks(date, PageRequest.of(0, 3)));
        }

        @Test
        @DisplayName("searchRanks propagates NumberFormatException for invalid member decoding")
        void searchRanks_invalidMember_throwsNfe() {
            // given
            LocalDate date = LocalDate.of(2025, 2, 4);
            PageRequest pageableInput = PageRequest.of(1, 1);
            String day = date.format(DateTimeFormatter.BASIC_ISO_DATE);
            String expectedKey = "metric.product.all:" + day;

            LinkedHashSet<String> encoded = new LinkedHashSet<>();
            encoded.add("abc"); // not numeric; invert9sComplement will not yield a numeric-only string

            when(zSet.reverseRange(eq(expectedKey), anyLong(), anyLong())).thenReturn(encoded);

            // when/then
            assertThrows(NumberFormatException.class, () -> repository.searchRanks(date, pageableInput));

            // zCard not called due to early failure while mapping members
            verify(zSet, never()).zCard(anyString());
            verify(zSet).reverseRange(eq(expectedKey), anyLong(), anyLong());
            verifyNoMoreInteractions(zSet);
        }
    }
}