package com.loopers.application.ranking;

import com.loopers.domain.product.ProductResult;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.ranking.RankingCommand;
import com.loopers.domain.ranking.RankingResult;
import com.loopers.domain.ranking.RankingService;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test framework: JUnit Jupiter (JUnit 5) + Mockito.
 * These tests validate RankingFacade.searchRankings across happy paths, empty results, and missing product details.
 */
class RankingFacadeTest {

    private RankingService rankingService;
    private ProductService productService;
    private RankingFacade facade;

    @BeforeEach
    void setUp() {
        rankingService = mock(RankingService.class);
        productService = mock(ProductService.class);
        facade = new RankingFacade(rankingService, productService);
    }

    @Nested
    @DisplayName("searchRankings - command wiring and happy path")
    class HappyPath {

        @Test
        void returnsOutputFromRanksAndProductDetails_whenItemsPresent() {
            // Arrange
            LocalDate date = LocalDate.of(2024, 12, 25);
            int page = 2, size = 3;

            // Fake rank items with productIds 11, 22, 33
            var rank1 = new RankingResult.SearchRanks.Item(1, 11L);
            var rank2 = new RankingResult.SearchRanks.Item(2, 22L);
            var rank3 = new RankingResult.SearchRanks.Item(3, 33L);
            var ranks = new RankingResult.SearchRanks(date, page, size, 9, List.of(rank1, rank2, rank3));

            when(rankingService.searchRanks(any(RankingCommand.SearchRanks.class))).thenReturn(ranks);

            var d1 = new ProductResult.GetProductDetail(11L, "p11", "desc11", 100L, 10);
            var d2 = new ProductResult.GetProductDetail(22L, "p22", "desc22", 200L, 20);
            var d3 = new ProductResult.GetProductDetail(33L, "p33", "desc33", 300L, 30);

            when(productService.getProductDetail(11L)).thenReturn(Optional.of(d1));
            when(productService.getProductDetail(22L)).thenReturn(Optional.of(d2));
            when(productService.getProductDetail(33L)).thenReturn(Optional.of(d3));

            var input = new RankingInput.SearchRankings(date, page, size);

            // Act
            var output = facade.searchRankings(input);

            // Assert - command wiring
            ArgumentCaptor<RankingCommand.SearchRanks> captor = ArgumentCaptor.forClass(RankingCommand.SearchRanks.class);
            verify(rankingService).searchRanks(captor.capture());
            var sent = captor.getValue();
            assertEquals(date, sent.date());
            assertEquals(page, sent.page());
            assertEquals(size, sent.size());

            // Assert - product details fetched for each rank
            verify(productService).getProductDetail(11L);
            verify(productService).getProductDetail(22L);
            verify(productService).getProductDetail(33L);
            verifyNoMoreInteractions(productService);

            // Basic sanity on output
            assertNotNull(output, "Output must not be null");
        }
    }

    @Nested
    @DisplayName("searchRankings - empty items")
    class EmptyItems {

        @Test
        void returnsEmptyOutputAndDoesNotQueryProductService_whenNoItems() {
            // Arrange
            LocalDate date = LocalDate.of(2024, 1, 1);
            int page = 0, size = 10;
            var ranks = new RankingResult.SearchRanks(date, page, size, 0, List.of());
            when(rankingService.searchRanks(any(RankingCommand.SearchRanks.class))).thenReturn(ranks);

            var input = new RankingInput.SearchRankings(date, page, size);

            // Act
            var output = facade.searchRankings(input);

            // Assert
            assertNotNull(output, "Output should not be null even when empty");
            verifyNoInteractions(productService);
        }
    }

    @Nested
    @DisplayName("searchRankings - missing product detail")
    class MissingProductDetail {

        @Test
        void throwsBusinessException_whenProductDetailNotFound() {
            // Arrange
            LocalDate date = LocalDate.of(2024, 6, 30);
            var rank = new RankingResult.SearchRanks.Item(1, 999L);
            var ranks = new RankingResult.SearchRanks(date, 0, 1, 1, List.of(rank));
            when(rankingService.searchRanks(any(RankingCommand.SearchRanks.class))).thenReturn(ranks);
            when(productService.getProductDetail(999L)).thenReturn(Optional.empty());

            var input = new RankingInput.SearchRankings(date, 0, 1);

            // Act + Assert
            BusinessException ex = assertThrows(BusinessException.class, () -> facade.searchRankings(input));
            assertEquals(CommonErrorType.NOT_FOUND, ex.getErrorType());
            verify(productService).getProductDetail(999L);
        }
    }
}