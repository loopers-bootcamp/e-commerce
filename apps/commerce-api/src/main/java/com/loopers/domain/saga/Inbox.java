package com.loopers.domain.saga;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import io.hypersistence.utils.hibernate.type.json.JsonStringType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Map;

@Getter
@Entity
@Table(
        name = "inboxes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"event_key", "event_name"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inbox extends BaseEntity {

    /**
     * 아이디
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inbox_id", nullable = false, updatable = false)
    private Long id;

    /**
     * 이벤트 키
     */
    @Column(name = "event_key", nullable = false, updatable = false)
    private String eventKey;

    /**
     * 이벤트 이름
     */
    @Column(name = "event_name", nullable = false, updatable = false)
    private String eventName;

    /**
     * 데이터
     */
    @Type(JsonStringType.class)
    @Column(name = "payload", updatable = false)
    private Map<String, Object> payload;

    // -------------------------------------------------------------------------------------------------

    @Builder
    private Inbox(String eventKey, String eventName, Map<String, Object> payload) {
        if (!StringUtils.hasText(eventKey)) {
            throw new BusinessException(CommonErrorType.INVALID, "올바르지 않은 이벤트 키입니다.");
        }

        if (eventName == null) {
            throw new BusinessException(CommonErrorType.INVALID, "올바르지 않은 이벤트 이름입니다.");
        }

        this.eventKey = eventKey;
        this.eventName = eventName;
        this.payload = payload == null ? null : Collections.unmodifiableMap(payload);
    }

}
