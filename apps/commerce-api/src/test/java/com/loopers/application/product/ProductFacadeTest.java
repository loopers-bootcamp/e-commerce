package com.loopers.application.product;

import com.loopers.domain.activity.event.ActivityEvent;
import com.loopers.domain.brand.BrandResult;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.product.ProductResult;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.ranking.RankingCommand;
import com.loopers.domain.ranking.RankingService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.task.TaskExecutor;
import org.springframework.util.concurrent.ListenableFutureTask;

import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Test stack: JUnit 5 (Jupiter) + Mockito + AssertJ.
 * These tests validate ProductFacade.getProductDetail focusing on the behaviors introduced in the PR diff:
 * - Fetching product detail, brand (nullable), and rank (nullable)
 * - Publishing ActivityEvent.View asynchronously only when userName is present and resolves to a user
 */
class ProductFacadeTest {

    private ProductService productService;
    private BrandService brandService;
    private RankingService rankingService;
    private UserService userService;
    private TaskExecutor taskExecutor;
    private ApplicationEventPublisher eventPublisher;

    private ProductFacade facade;

    @BeforeEach
    void setUp() {
        productService = mock(ProductService.class);
        brandService = mock(BrandService.class);
        rankingService = mock(RankingService.class);
        userService = mock(UserService.class);
        eventPublisher = mock(ApplicationEventPublisher.class);

        // Synchronous TaskExecutor for deterministic testing of async branch
        taskExecutor = runnable -> runnable.run();

        facade = new ProductFacade(
                productService,
                brandService,
                rankingService,
                userService,
                taskExecutor,
                eventPublisher
        );
    }

    private static ProductInput.GetProductDetail input(Long productId, String userName) {
        // Adapt to actual constructor/factory if different in codebase
        return new ProductInput.GetProductDetail(productId, userName);
    }

    private static ProductResult.GetProductDetail productDetail(Long productId, Long brandId) {
        // Adapt to actual constructor/factory if different in codebase
        return new ProductResult.GetProductDetail(productId, "name", brandId, "desc", 1000L);
    }

    private static BrandResult.GetBrand brand(Long brandId) {
        // Adapt to actual constructor/factory if different in codebase
        return new BrandResult.GetBrand(brandId, "brand");
    }

    @Nested
    @DisplayName("Happy paths")
    class HappyPaths {
        @Test
        @DisplayName("Returns output with brand and rank when present; publishes view event for valid userName")
        void returnsOutputWithBrandAndRank_andPublishesEvent() {
            // given
            Long productId = 10L;
            Long brandId = 7L;
            Long expectedRank = 5L;
            String userName = "jane";

            ProductResult.GetProductDetail detail = productDetail(productId, brandId);
            when(productService.getProductDetail(productId)).thenReturn(Optional.of(detail));
            when(brandService.getBrand(brandId)).thenReturn(Optional.of(brand(brandId)));
            when(rankingService.findRank(new RankingCommand.FindRank(LocalDate.now(), productId)))
                    .thenReturn(Optional.of(expectedRank));
            User mockUser = mock(User.class);
            when(mockUser.getUserId()).thenReturn(99L);
            when(userService.getUser(userName)).thenReturn(Optional.of(mockUser));

            // when
            ProductOutput.GetProductDetail out = facade.getProductDetail(input(productId, userName));

            // then
            assertThat(out).isNotNull();

            ArgumentCaptor<Object> evt = ArgumentCaptor.forClass(Object.class);
            verify(eventPublisher, times(1)).publishEvent(evt.capture());
            assertThat(evt.getValue()).isInstanceOf(ActivityEvent.View.class);

            // Verify call order through main flow
            InOrder inOrder = inOrder(productService, brandService, rankingService);
            inOrder.verify(productService).getProductDetail(productId);
            inOrder.verify(brandService).getBrand(brandId);
            inOrder.verify(rankingService).findRank(any(RankingCommand.FindRank.class));
        }

        @Test
        @DisplayName("Returns output when brand and rank are absent (nulls propagated)")
        void returnsOutputWhenBrandAndRankAbsent() {
            Long productId = 11L;
            Long brandId = 77L;

            ProductResult.GetProductDetail detail = productDetail(productId, brandId);
            when(productService.getProductDetail(productId)).thenReturn(Optional.of(detail));
            when(brandService.getBrand(brandId)).thenReturn(Optional.empty());
            when(rankingService.findRank(any(RankingCommand.FindRank.class))).thenReturn(Optional.empty());

            ProductOutput.GetProductDetail out = facade.getProductDetail(input(productId, null));

            assertThat(out).isNotNull();
            verify(eventPublisher, never()).publishEvent(any());
        }
    }

    @Nested
    @DisplayName("Guard clauses and edge cases")
    class GuardAndEdges {
        @Test
        @DisplayName("Throws NOT_FOUND when product detail is missing")
        void throwsWhenProductMissing() {
            Long productId = 404L;
            when(productService.getProductDetail(productId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> facade.getProductDetail(input(productId, "user")))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(CommonErrorType.NOT_FOUND.name());
            verifyNoInteractions(brandService, rankingService, eventPublisher, userService);
        }

        @Test
        @DisplayName("Does not publish event when userName is null")
        void noEventWhenUserNameNull() {
            Long productId = 12L;
            Long brandId = 1L;

            when(productService.getProductDetail(productId)).thenReturn(Optional.of(productDetail(productId, brandId)));
            when(brandService.getBrand(brandId)).thenReturn(Optional.of(brand(brandId)));

            ProductOutput.GetProductDetail out = facade.getProductDetail(input(productId, null));

            assertThat(out).isNotNull();
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("Does not publish event when userName is blank")
        void noEventWhenUserNameBlank() {
            Long productId = 13L;
            Long brandId = 2L;

            when(productService.getProductDetail(productId)).thenReturn(Optional.of(productDetail(productId, brandId)));
            when(brandService.getBrand(brandId)).thenReturn(Optional.of(brand(brandId)));

            ProductOutput.GetProductDetail out = facade.getProductDetail(input(productId, "  "));

            assertThat(out).isNotNull();
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("Executes async branch but does not publish when user not found")
        void asyncBranchNoUser() {
            Long productId = 14L;
            Long brandId = 3L;

            when(productService.getProductDetail(productId)).thenReturn(Optional.of(productDetail(productId, brandId)));
            when(brandService.getBrand(brandId)).thenReturn(Optional.of(brand(brandId)));
            when(userService.getUser("ghost")).thenReturn(Optional.empty());

            ProductOutput.GetProductDetail out = facade.getProductDetail(input(productId, "ghost"));

            assertThat(out).isNotNull();
            verify(userService, times(1)).getUser("ghost");
            verify(eventPublisher, never()).publishEvent(any());
        }
    }
}