package com.loopers.infrastructure.audit;

import com.loopers.domain.audit.EventLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;

public interface EventLogJpaRepository extends JpaRepository<EventLog, Long> {

    @Modifying
    @Query("""
                insert into EventLog (id, eventKey, eventName, userId, createdAt, updatedAt)
                values (:eventId, :eventKey, :eventName, :userId, :createdAt, :updatedAt)
                on conflict (id) do nothing
            """)
    int insertIfNotExists(
            @Param("eventId") String eventId,
            @Param("eventKey") String eventKey,
            @Param("eventName") String eventName,
            @Param("userId") Long userId,
            @Param("createdAt") ZonedDateTime createdAt,
            @Param("updatedAt") ZonedDateTime updatedAt
    );

}
