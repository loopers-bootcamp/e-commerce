package com.loopers.domain.saga;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Entity
@Table(
        name = "outboxes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"event_key", "event_name"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Outbox extends BaseEntity {

    /**
     * 아이디
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "outbox_id", nullable = false, updatable = false)
    private Long id;

    /**
     * 이벤트 키
     */
    @Column(name = "event_key", nullable = false, updatable = false)
    private UUID eventKey;

    /**
     * 이벤트 이름
     */
    @Column(name = "event_name", nullable = false, updatable = false)
    private String eventName;

    /**
     * 데이터
     */
    @Column(name = "payload", updatable = false)
    private String payload;

    // -------------------------------------------------------------------------------------------------

    @Builder
    private Outbox(UUID eventKey, String eventName, String payload) {
        if (eventKey == null) {
            throw new BusinessException(CommonErrorType.INVALID, "올바르지 않은 이벤트 키입니다.");
        }

        if (eventName == null) {
            throw new BusinessException(CommonErrorType.INVALID, "올바르지 않은 이벤트 이름입니다.");
        }

        this.eventKey = eventKey;
        this.eventName = eventName;
        this.payload = payload;
    }

}
