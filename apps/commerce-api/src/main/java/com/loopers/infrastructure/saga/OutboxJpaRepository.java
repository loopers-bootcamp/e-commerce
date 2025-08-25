package com.loopers.infrastructure.saga;

import com.loopers.domain.saga.Outbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.UUID;

public interface OutboxJpaRepository extends JpaRepository<Outbox, UUID> {

    @Modifying
    @Query("""
                insert into Outbox (eventKey, eventName, payload, createdAt, updatedAt)
                values (:eventKey, :eventName, :payload, :createdAt, :updatedAt)
                on conflict (eventKey, eventName) do nothing
            """)
    int insertIfNotExists(
            @Param("eventKey") UUID eventKey,
            @Param("eventName") String eventName,
            @Param("payload") String payload,
            @Param("createdAt") ZonedDateTime createdAt,
            @Param("updatedAt") ZonedDateTime updatedAt
    );

}
