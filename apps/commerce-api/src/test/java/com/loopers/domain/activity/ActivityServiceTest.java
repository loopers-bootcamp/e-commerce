package com.loopers.domain.activity;

import com.loopers.domain.activity.event.ActivityEvent;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.root;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@MockitoSettings
class ActivityServiceTest {

    @InjectMocks
    private ActivityService sut;

    @Mock
    private LikedProductRepository likedProductRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @DisplayName("좋아요 수를 조회할 때:")
    @Nested
    class GetLikeCount {

        @DisplayName("주어진 상품 아이디의 좋아요 수를 반환한다.")
        @Test
        void returnLikeCount_withProductId() {
            // given
            Long productId = Instancio.create(Long.class);
            Long initialLikeCount = Instancio.of(Long.class)
                    .generate(root(), gen -> gen.longs().range(0L, 10_000L))
                    .create();

            given(likedProductRepository.countByProductId(productId))
                    .willReturn(initialLikeCount);

            // when
            long likeCount = sut.getLikeCount(productId);

            // then
            assertThat(likeCount).isEqualTo(initialLikeCount);

            verify(likedProductRepository, times(1)).countByProductId(productId);
        }

    }

    // -------------------------------------------------------------------------------------------------

    @DisplayName("상품에 좋아요를 표시할 때:")
    @Nested
    class Like {

        @DisplayName("좋아요 한 상품이 아니라면, LikedProduct가 생성된다.")
        @Test
        void createNewLikedProduct_whenNonexistent() {
            // given
            Long userId = Instancio.create(Long.class);
            Long productId = Instancio.create(Long.class);

            ActivityCommand.Like command = ActivityCommand.Like.builder()
                    .userId(userId)
                    .productId(productId)
                    .build();

            given(likedProductRepository.saveIfAbsent(any(LikedProduct.class)))
                    .willReturn(true);

            // when
            sut.like(command);

            // then
            verify(likedProductRepository, times(1)).saveIfAbsent(any(LikedProduct.class));
            verify(eventPublisher, times(1)).publishEvent(any(ActivityEvent.Like.class));
        }

        @DisplayName("이미 좋아요 한 상품이라면, 아무것도 하지 않는다. (멱등성 보장)")
        @Test
        void doNothing_whenAlreadyLiked() {
            // given
            Long userId = Instancio.create(Long.class);
            Long productId = Instancio.create(Long.class);

            ActivityCommand.Like command = ActivityCommand.Like.builder()
                    .userId(userId)
                    .productId(productId)
                    .build();

            given(likedProductRepository.saveIfAbsent(any(LikedProduct.class)))
                    .willReturn(false);

            // when
            sut.like(command);

            // then
            verify(likedProductRepository, times(1)).saveIfAbsent(any(LikedProduct.class));
            verify(eventPublisher, never()).publishEvent(any(ActivityEvent.Like.class));
        }

    }

    // -------------------------------------------------------------------------------------------------

    @DisplayName("상품에 좋아요를 취소할 때:")
    @Nested
    class Dislike {

        @DisplayName("좋아요 한 상품이라면, LikedProduct가 삭제된다.")
        @Test
        void deleteLikedProduct_whenAlreadyLiked() {
            // given
            Long userId = Instancio.create(Long.class);
            Long productId = Instancio.create(Long.class);

            ActivityCommand.Dislike command = ActivityCommand.Dislike.builder()
                    .userId(userId)
                    .productId(productId)
                    .build();

            given(likedProductRepository.deleteIfPresent(any(LikedProduct.class)))
                    .willReturn(true);

            // when
            sut.dislike(command);

            // then
            verify(likedProductRepository, times(1)).deleteIfPresent(any(LikedProduct.class));
            verify(eventPublisher, times(1)).publishEvent(any(ActivityEvent.Dislike.class));
        }

        @DisplayName("좋아요 한 상품이 아니라면, 아무것도 하지 않는다. (멱등성 보장)")
        @Test
        void doNothing_whenNonexistent() {
            // given
            Long userId = Instancio.create(Long.class);
            Long productId = Instancio.create(Long.class);

            ActivityCommand.Dislike command = ActivityCommand.Dislike.builder()
                    .userId(userId)
                    .productId(productId)
                    .build();

            given(likedProductRepository.deleteIfPresent(any(LikedProduct.class)))
                    .willReturn(false);


            // when
            sut.dislike(command);

            // then
            verify(likedProductRepository, times(1)).deleteIfPresent(any(LikedProduct.class));
            verify(eventPublisher, never()).publishEvent(any(ActivityEvent.Dislike.class));
        }

    }

}
