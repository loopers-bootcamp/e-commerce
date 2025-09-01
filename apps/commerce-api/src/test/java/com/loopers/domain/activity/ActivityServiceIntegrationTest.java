package com.loopers.domain.activity;

import com.loopers.utils.DatabaseCleanUp;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.instancio.Instancio;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static com.loopers.test.assertion.ConcurrentAssertion.assertThatConcurrence;
import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.root;

@SpringBootTest
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class ActivityServiceIntegrationTest {

    private final ActivityService sut;

    @MockitoBean
    private final ApplicationEventPublisher eventPublisher;

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
            int threadCount = 10;

            Long userId = Instancio.create(Long.class);
            Long productId = Instancio.create(Long.class);

            ActivityCommand.Like command = ActivityCommand.Like.builder()
                    .userId(userId)
                    .productId(productId)
                    .build();

            // when & then
            assertThatConcurrence()
                    .withThreadCount(threadCount)
                    .isExecutedBy(() -> sut.like(command))
                    .isDone()
                    .hasNoError();

            // then
            long likeCount = entityManager
                    .createQuery("SELECT count(pl) FROM LikedProduct pl WHERE pl.userId = :userId AND pl.productId = :productId", long.class)
                    .setParameter("userId", userId)
                    .setParameter("productId", productId)
                    .getSingleResult();
            assertThat(likeCount).isOne();
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
            int threadCount = 10;
            Long userId = Instancio.create(Long.class);
            Long productId = Instancio.create(Long.class);

            ActivityCommand.Dislike command = ActivityCommand.Dislike.builder()
                    .userId(userId)
                    .productId(productId)
                    .build();

            // when & then
            assertThatConcurrence()
                    .withThreadCount(threadCount)
                    .isExecutedBy(() -> sut.dislike(command))
                    .isDone()
                    .hasNoError();

            // then
            long likeCount = entityManager
                    .createQuery("SELECT count(pl) FROM LikedProduct pl WHERE pl.userId = :userId AND pl.productId = :productId", long.class)
                    .setParameter("userId", userId)
                    .setParameter("productId", productId)
                    .getSingleResult();
            assertThat(likeCount).isZero();
        }

    }

    // -------------------------------------------------------------------------------------------------

    @DisplayName("동일한 상품에 좋아요를 표시/취소할 때:")
    @Nested
    class LikeAndDislike {

        @DisplayName("여러 사용자가 동시에 좋아요 표시와 좋아요 취소를 요청해도, 좋아요 수는 정상 반영이 된다.")
        @RepeatedTest(5)
        void manageLikeCountSuccessfully_whenUsersRequestToLikeAndDislikeConcurrently() {
            // given
            List<Long> userIds = Instancio.stream(Long.class)
                    .distinct()
                    .limit(50)
                    .toList();
            Long productId = Instancio.create(Long.class);

            Integer threshold = Instancio.of(Integer.class)
                    .generate(root(), gen -> gen.ints().range(0, userIds.size() - 1))
                    .create();
            transactionTemplate.executeWithoutResult(status ->
                    IntStream.range(threshold, userIds.size())
                            .mapToObj(i -> LikedProduct.builder()
                                    .userId(userIds.get(i))
                                    .productId(productId)
                                    .build()
                            )
                            .forEach(entityManager::persist));

            // when & then
            assertThatConcurrence()
                    .withThreadCount(userIds.size())
                    .isExecutedBy(i -> {
                        Long userId = userIds.get(i);

                        if (i < threshold) {
                            ActivityCommand.Like command = ActivityCommand.Like.builder()
                                    .userId(userId)
                                    .productId(productId)
                                    .build();
                            sut.like(command);
                        } else {
                            ActivityCommand.Dislike command = ActivityCommand.Dislike.builder()
                                    .userId(userId)
                                    .productId(productId)
                                    .build();
                            sut.dislike(command);
                        }
                    })
                    .isDone()
                    .hasNoError();

            int expectedLikeCount = threshold;
            long likeCount = entityManager
                    .createQuery("SELECT count(pl) FROM LikedProduct pl WHERE pl.productId = :productId", long.class)
                    .setParameter("productId", productId)
                    .getSingleResult();
            assertThat(likeCount).isEqualTo(expectedLikeCount);
        }

    }

}
