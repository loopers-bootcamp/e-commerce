package com.loopers.infrastructure.product;

import com.loopers.domain.product.Stock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;

public interface StockJpaRepository extends JpaRepository<Stock, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Stock> findByProductOptionIdIn(List<Long> productOptionIds);

}
