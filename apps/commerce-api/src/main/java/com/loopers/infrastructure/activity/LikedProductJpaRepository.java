package com.loopers.infrastructure.activity;

import com.loopers.domain.activity.LikedProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.Optional;

public interface LikedProductJpaRepository extends JpaRepository<LikedProduct, Long> {

    long countByProductId(Long productId);

    @Modifying
    @Query("""
                insert into LikedProduct (userId, productId, createdAt, updatedAt)
                values (:userId, :productId, :createdAt, :updatedAt)
                on conflict (userId, productId) do nothing
            """)
    int insertIfNotExists(
            @Param("userId") Long userId,
            @Param("productId") Long productId,
            @Param("createdAt") ZonedDateTime createdAt,
            @Param("updatedAt") ZonedDateTime updatedAt
    );

    @Modifying
    @Query("""
                delete
                 from LikedProduct lp
                where lp.userId = :userId
                  and lp.productId = :productId
            """)
    int delete(
            @Param("userId") Long userId,
            @Param("productId") Long productId
    );

    Optional<LikedProduct> findByUserIdAndProductId(Long userId, Long productId);

}
