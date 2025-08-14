package com.loopers.infrastructure.product.rdb;

import com.loopers.domain.product.ProductStock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;

public interface StockJpaRepository extends JpaRepository<ProductStock, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<ProductStock> findByProductOptionIdIn(List<Long> productOptionIds);

}
