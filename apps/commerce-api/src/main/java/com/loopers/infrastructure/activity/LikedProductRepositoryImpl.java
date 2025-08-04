package com.loopers.infrastructure.activity;

import com.loopers.domain.activity.ActivityQueryResult;
import com.loopers.domain.activity.LikedProduct;
import com.loopers.domain.activity.LikedProductRepository;
import com.loopers.domain.activity.QLikedProduct;
import com.loopers.domain.product.QProduct;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LikedProductRepositoryImpl implements LikedProductRepository {

    private final LikedProductJpaRepository likedProductJpaRepository;
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<ActivityQueryResult.GetLikedProducts> findByUserId(Long userId) {
        QLikedProduct lp = QLikedProduct.likedProduct;
        QProduct p = QProduct.product;

        return jpaQueryFactory
                .select(
                        lp.id
                        , lp.userId
                        , lp.productId
                        , p.name
                )
                .from(lp)
                .join(p).on(p.id.eq(lp.productId))
                .where(lp.userId.eq(userId))
                .stream()
                .map(row -> ActivityQueryResult.GetLikedProducts.builder()
                        .likedProductId(row.get(lp.id))
                        .userId(row.get(lp.userId))
                        .productId(row.get(lp.productId))
                        .productName(row.get(p.name))
                        .build()
                )
                .toList();
    }

    @Override
    public long countByProductId(Long productId) {
        return likedProductJpaRepository.countByProductId(productId);
    }

    @Override
    public Optional<LikedProduct> findOne(Long userId, Long productId) {
        return likedProductJpaRepository.findByUserIdAndProductId(userId, productId);
    }

    @Override
    public LikedProduct save(LikedProduct likedProduct) {
        return likedProductJpaRepository.save(likedProduct);
    }

    @Override
    public void delete(LikedProduct likedProduct) {
        likedProductJpaRepository.delete(likedProduct);
    }

}
