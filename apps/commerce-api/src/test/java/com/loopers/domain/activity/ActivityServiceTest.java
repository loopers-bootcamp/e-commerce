package com.loopers.domain.activity;

import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;

import java.util.Optional;

import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@MockitoSettings
class ActivityServiceTest {

    @InjectMocks
    private ActivityService sut;

    @Mock
    private LikedProductRepository likedProductRepository;

    @DisplayName("상품에 좋아요를 표시할 때:")
    @Nested
    class Like {

        @DisplayName("좋아요 한 상품이 아니라면, LikedProduct가 생성된다.")
        @Test
        void createNewLikedProduct_whenNonexistent() {
            // given
            Long userId = Instancio.create(Long.class);
            Long productId = Instancio.create(Long.class);

            given(likedProductRepository.findOne(userId, productId))
                    .willReturn(Optional.empty());
            given(likedProductRepository.save(any(LikedProduct.class)))
                    .willAnswer(invocation -> {
                        LikedProduct saved = invocation.getArgument(0);
                        return Instancio.of(LikedProduct.class)
                                .set(field(LikedProduct::getUserId), saved.getUserId())
                                .set(field(LikedProduct::getProductId), saved.getProductId())
                                .create();
                    });

            ActivityCommand.Like command = ActivityCommand.Like.builder()
                    .userId(userId)
                    .productId(productId)
                    .build();

            // when
            sut.like(command);

            // then
            verify(likedProductRepository, times(1)).findOne(userId, productId);
            verify(likedProductRepository, times(1)).save(any(LikedProduct.class));
        }

        @DisplayName("이미 좋아요 한 상품이라면, 아무것도 하지 않는다. (멱등성 보장)")
        @Test
        void doNothing_whenAlreadyLiked() {
            // given
            Long userId = Instancio.create(Long.class);
            Long productId = Instancio.create(Long.class);

            given(likedProductRepository.findOne(userId, productId))
                    .willAnswer(invocation ->
                            Instancio.of(LikedProduct.class)
                                    .set(field(LikedProduct::getUserId), userId)
                                    .set(field(LikedProduct::getProductId), productId)
                                    .stream().findAny()
                    );


            ActivityCommand.Like command = ActivityCommand.Like.builder()
                    .userId(userId)
                    .productId(productId)
                    .build();


            // when
            sut.like(command);

            // then
            verify(likedProductRepository, times(1)).findOne(userId, productId);
            verify(likedProductRepository, never()).save(any(LikedProduct.class));
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

            given(likedProductRepository.findOne(userId, productId))
                    .willAnswer(invocation ->
                            Instancio.of(LikedProduct.class)
                                    .set(field(LikedProduct::getUserId), userId)
                                    .set(field(LikedProduct::getProductId), productId)
                                    .stream().findAny()
                    );

            ActivityCommand.Dislike command = ActivityCommand.Dislike.builder()
                    .userId(userId)
                    .productId(productId)
                    .build();

            // when
            sut.dislike(command);

            // then
            verify(likedProductRepository, times(1)).findOne(userId, productId);
            verify(likedProductRepository, times(1)).delete(any(LikedProduct.class));
        }

        @DisplayName("좋아요 한 상품이 아니라면, 아무것도 하지 않는다. (멱등성 보장)")
        @Test
        void doNothing_whenNonexistent() {
            // given
            Long userId = Instancio.create(Long.class);
            Long productId = Instancio.create(Long.class);

            given(likedProductRepository.findOne(userId, productId))
                    .willReturn(Optional.empty());

            ActivityCommand.Dislike command = ActivityCommand.Dislike.builder()
                    .userId(userId)
                    .productId(productId)
                    .build();

            // when
            sut.dislike(command);

            // then
            verify(likedProductRepository, times(1)).findOne(userId, productId);
            verify(likedProductRepository, never()).delete(any(LikedProduct.class));
        }

    }

}
