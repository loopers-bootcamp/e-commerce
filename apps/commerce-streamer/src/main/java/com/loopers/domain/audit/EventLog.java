package com.loopers.domain.audit;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "event_logs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventLog extends BaseEntity {

    /**
     * 아이디
     */
    @Id
    @Column(name = "event_log_id", nullable = false, updatable = false)
    private String id;

    /**
     * 이름
     */
    @Column(name = "event_key", nullable = false)
    private String eventKey;

    /**
     * 이름
     */
    @Column(name = "event_name", nullable = false)
    private String eventName;

    /**
     * 성별
     */
    @Column(name = "ref_user_id", nullable = false, updatable = false)
    private Long userId;

    // -------------------------------------------------------------------------------------------------

    @Builder
    private EventLog(String id, String eventKey, String eventName, Long userId) {
        this.id = id;
        this.eventKey = eventKey;
        this.eventName = eventName;
        this.userId = userId;
    }

}
