package com.loopers.config.jpa;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
class QueryDslConfig {

    @Bean
    @Primary
    JPAQueryFactory mysqlJpaQueryFactory(EntityManager entityManager) {
        return new JPAQueryFactory(entityManager);
    }

}
