package com.loopers.infrastructure.saga;

import com.loopers.domain.saga.Inbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.UUID;

public interface InboxJpaRepository extends JpaRepository<Inbox, UUID> {

    @Modifying
    @Query("""
                insert into Inbox (eventKey, eventName, payload, createdAt, updatedAt)
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
