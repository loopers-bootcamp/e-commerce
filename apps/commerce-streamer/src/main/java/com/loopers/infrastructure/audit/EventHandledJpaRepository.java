package com.loopers.infrastructure.audit;

import com.loopers.domain.audit.EventHandled;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;

public interface EventHandledJpaRepository extends JpaRepository<EventHandled, String> {

    @Modifying
    @Query("""
                insert into EventHandled (id, topicName, createdAt, updatedAt)
                values (:eventId, :topicName, :createdAt, :updatedAt)
                on conflict (id) do nothing
            """)
    int insertIfNotExists(
            @Param("eventId") String eventId,
            @Param("topicName") String topicName,
            @Param("createdAt") ZonedDateTime createdAt,
            @Param("updatedAt") ZonedDateTime updatedAt
    );

}
