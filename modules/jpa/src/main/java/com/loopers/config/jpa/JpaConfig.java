package com.loopers.config.jpa;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * <h3>
 * <code>&#064;EnableJpaRepositories(enableDefaultTransactions = false)</code>
 * </h3>
 * {@code SimpleJpaRepository}에 <code>&#064;Transactional</code>이 붙어져 있다.
 * {@code findById}, {@code findAll}을 사용하면 {@link com.loopers.annotation.ReadOnlyTransactional}을 사용해도
 * 읽기 전용 트랜잭션을 종료하가 위해, commit 요청을 보낸다. 이를 방지하기 위해 {@code JpaRepository}의 기본 트랜잭션을 비활성화한다.
 * <p>
 * 비활성화하면 {@code CrudRepository}의 메서드를 실행하는데, 여기에는 애노테이션이 붙어있지 않다.
 * 따라서 {@code save}, {@code saveAll}를 호출할 때 트랜잭션 내에서 실행해야 한다.
 */
@Configuration
@EnableTransactionManagement
@EntityScan(basePackages = "com.loopers")
@EnableJpaRepositories(
        basePackages = "com.loopers.infrastructure",
        enableDefaultTransactions = false
)
public class JpaConfig {
}
