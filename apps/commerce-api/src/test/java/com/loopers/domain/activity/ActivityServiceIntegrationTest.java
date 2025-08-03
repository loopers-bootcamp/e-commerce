package com.loopers.domain.activity;

import com.loopers.utils.DatabaseCleanUp;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.assertj.core.data.TemporalUnitLessThanOffset;
import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.root;

@SpringBootTest
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class ActivityServiceIntegrationTest {

    private final ActivityService sut;

    private final TransactionTemplate transactionTemplate;
    private final EntityManager entityManager;
    private final DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("좋아요 수를 조회할 때:")
    @Nested
    class GetLikeCount {

        @DisplayName("주어진 상품 아이디의 좋아요 수를 반환한다.")
        @Test
        void returnLikeCount_withProductId() {
            // given
            int initialLikeCount = new Random().nextInt(1, 10);
            Long productId = Instancio.of(Long.class)
                    .generate(root(), gen -> gen.longs().range(1000L, 10_000L))
                    .create();

            transactionTemplate.executeWithoutResult(status ->
                    IntStream.range(0, initialLikeCount)
                            .mapToObj(i -> LikedProduct.builder()
                                    .userId(i + 1L)
                                    .productId(productId)
                                    .build()
                            )
                            .forEach(entityManager::persist));

            // when
            long likeCount = sut.getLikeCount(productId);

            // then
            assertThat(likeCount).isEqualTo(initialLikeCount);
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

            long initialLikeCount = entityManager
                    .createQuery("SELECT count(pl) FROM LikedProduct pl WHERE pl.userId = :userId AND pl.productId = :productId", long.class)
                    .setParameter("userId", userId)
                    .setParameter("productId", productId)
                    .getSingleResult();
            assertThat(initialLikeCount).isZero();

            ActivityCommand.Like command = ActivityCommand.Like.builder()
                    .userId(userId)
                    .productId(productId)
                    .build();

            // when
            sut.like(command);

            // then
            long actualLikeCount = entityManager
                    .createQuery("SELECT count(pl) FROM LikedProduct pl WHERE pl.userId = :userId AND pl.productId = :productId", long.class)
                    .setParameter("userId", userId)
                    .setParameter("productId", productId)
                    .getSingleResult();
            assertThat(actualLikeCount).isOne();
        }

        @DisplayName("이미 좋아요 한 상품이라면, 아무것도 하지 않는다. (멱등성 보장)")
        @Test
        void doNothing_whenAlreadyLiked() {
            // given
            Long userId = Instancio.create(Long.class);
            Long productId = Instancio.create(Long.class);

            LikedProduct initialLikedProduct = LikedProduct.builder()
                    .userId(userId)
                    .productId(productId)
                    .build();
            transactionTemplate.executeWithoutResult(status -> entityManager.persist(initialLikedProduct));

            ActivityCommand.Like command = ActivityCommand.Like.builder()
                    .userId(userId)
                    .productId(productId)
                    .build();

            // when
            sut.like(command);

            // then
            LikedProduct foundLikedProduct = entityManager
                    .createQuery("SELECT pl FROM LikedProduct pl WHERE pl.userId = :userId AND pl.productId = :productId", LikedProduct.class)
                    .setParameter("userId", userId)
                    .setParameter("productId", productId)
                    .getSingleResult();
            assertThat(foundLikedProduct).isNotNull();
            assertThat(foundLikedProduct.getId()).isEqualTo(initialLikedProduct.getId());
            assertThat(foundLikedProduct.getCreatedAt()).isCloseTo(initialLikedProduct.getCreatedAt(),
                    new TemporalUnitLessThanOffset(1, ChronoUnit.MILLIS));
            assertThat(foundLikedProduct.getUpdatedAt()).isCloseTo(initialLikedProduct.getUpdatedAt(),
                    new TemporalUnitLessThanOffset(1, ChronoUnit.MILLIS));
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

            LikedProduct initialLikedProduct = LikedProduct.builder()
                    .userId(userId)
                    .productId(productId)
                    .build();
            transactionTemplate.executeWithoutResult(status -> entityManager.persist(initialLikedProduct));

            ActivityCommand.Dislike command = ActivityCommand.Dislike.builder()
                    .userId(userId)
                    .productId(productId)
                    .build();

            // when
            sut.dislike(command);

            // then
            long likeCount = entityManager
                    .createQuery("SELECT count(pl) FROM LikedProduct pl WHERE pl.userId = :userId AND pl.productId = :productId", long.class)
                    .setParameter("userId", userId)
                    .setParameter("productId", productId)
                    .getSingleResult();
            assertThat(likeCount).isZero();
        }

        @DisplayName("좋아요 한 상품이 아니라면, 아무것도 하지 않는다. (멱등성 보장)")
        @Test
        void doNothing_whenNonexistent() {
            // given
            Long userId = Instancio.create(Long.class);
            Long productId = Instancio.create(Long.class);

            long initialLikeCount = entityManager
                    .createQuery("SELECT count(pl) FROM LikedProduct pl WHERE pl.userId = :userId AND pl.productId = :productId", long.class)
                    .setParameter("userId", userId)
                    .setParameter("productId", productId)
                    .getSingleResult();
            assertThat(initialLikeCount).isZero();

            ActivityCommand.Dislike command = ActivityCommand.Dislike.builder()
                    .userId(userId)
                    .productId(productId)
                    .build();

            // when
            sut.dislike(command);

            // then
            long actualLikeCount = entityManager
                    .createQuery("SELECT count(pl) FROM LikedProduct pl WHERE pl.userId = :userId AND pl.productId = :productId", long.class)
                    .setParameter("userId", userId)
                    .setParameter("productId", productId)
                    .getSingleResult();
            assertThat(actualLikeCount).isZero();
        }

    }

}
