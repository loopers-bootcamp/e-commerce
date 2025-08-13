package com.loopers.application.activity;

import com.loopers.domain.activity.LikedProduct;
import com.loopers.domain.product.Product;
import com.loopers.domain.user.User;
import com.loopers.domain.user.attribute.Email;
import com.loopers.utils.DatabaseCleanUp;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.stream.IntStream;

import static com.loopers.test.assertion.ConcurrentAssertion.assertThatConcurrence;
import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.instancio.Select.root;

@SpringBootTest
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class ActivityFacadeIntegrationTest {

    private final ActivityFacade sut;

    private final TransactionTemplate transactionTemplate;
    private final EntityManager entityManager;
    private final DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("동일한 상품에 좋아요를 표시/취소할 때:")
    @Nested
    class LikeAndDislike {

        @DisplayName("여러 사용자가 동시에 좋아요 표시와 좋아요 취소를 요청해도, 좋아요 수는 정상 반영이 된다.")
        @RepeatedTest(5)
        void manageLikeCountSuccessfully_whenUsersRequestToLikeAndDislikeConcurrently() {
            // given
            List<User> users = Instancio.ofList(User.class)
                    .size(50)
                    .ignore(field(User::getId))
                    .supply(field(User::getEmail),
                            () -> new Email(Instancio.gen().net().email().get()))
                    .create();
            transactionTemplate.executeWithoutResult(status -> users.forEach(entityManager::persist));

            Product product = Instancio.of(Product.class)
                    .ignore(field(Product::getId))
                    .set(field(Product::getName), "Nike Shoes 2025")
                    .set(field(Product::getBasePrice), 120_000)
                    .set(field(Product::getLikeCount), 100L)
                    .ignore(field(Product::getBrandId))
                    .create();
            transactionTemplate.executeWithoutResult(status -> entityManager.persist(product));

            Integer threshold = Instancio.of(Integer.class)
                    .generate(root(), gen -> gen.ints().range(0, users.size() - 1))
                    .create();
            transactionTemplate.executeWithoutResult(status ->
                    IntStream.range(threshold, users.size())
                            .mapToObj(i -> LikedProduct.builder()
                                    .userId(users.get(i).getId())
                                    .productId(product.getId())
                                    .build()
                            )
                            .forEach(entityManager::persist));

            // when & then
            assertThatConcurrence()
                    .withThreadCount(users.size())
                    .isExecutedBy(i -> {
                        User user = users.get(i);

                        if (i < threshold) {
                            ActivityInput.Like command = ActivityInput.Like.builder()
                                    .userName(user.getName())
                                    .productId(product.getId())
                                    .build();
                            sut.like(command);
                        } else {
                            ActivityInput.Dislike command = ActivityInput.Dislike.builder()
                                    .userName(user.getName())
                                    .productId(product.getId())
                                    .build();
                            sut.dislike(command);
                        }
                    })
                    .isDone()
                    .hasNoError();

            int requestedLikeCount = threshold;
            long likeCountFromLikedProduct = entityManager
                    .createQuery("SELECT count(pl) FROM LikedProduct pl WHERE pl.productId = :productId", long.class)
                    .setParameter("productId", product.getId())
                    .getSingleResult();
            assertThat(likeCountFromLikedProduct).isEqualTo(requestedLikeCount);

            long totalLikeCount = product.getLikeCount() + threshold - (users.size() - threshold);
            long likeCountFromProduct = entityManager
                    .createQuery("SELECT p.likeCount FROM Product p WHERE p.id = :id", long.class)
                    .setParameter("id", product.getId())
                    .getSingleResult();
            assertThat(likeCountFromProduct).isEqualTo(totalLikeCount);
        }

    }

}
