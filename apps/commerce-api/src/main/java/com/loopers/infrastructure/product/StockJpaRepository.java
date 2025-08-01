package com.loopers.infrastructure.product;

import com.loopers.domain.product.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockJpaRepository extends JpaRepository<Stock, Long> {

    List<Stock> findByProductOptionIdIn(List<Long> productOptionIds);

}
