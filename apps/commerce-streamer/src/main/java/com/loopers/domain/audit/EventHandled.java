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
import org.springframework.util.StringUtils;

@Getter
@Entity
@Table(name = "events_handled")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventHandled extends BaseEntity {

    /**
     * 아이디
     */
    @Id
    @Column(name = "event_id", nullable = false, updatable = false)
    private String id;

    /**
     * 토픽 이름
     */
    @Column(name = "topic_name", nullable = false, updatable = false)
    private String topicName;

    // -------------------------------------------------------------------------------------------------

    @Builder
    private EventHandled(String id, String topicName) {
        if (!StringUtils.hasText(id)) {
            throw new IllegalArgumentException("올바르지 않은 이벤트 아이디입니다.");
        }
        if (!StringUtils.hasText(topicName)) {
            throw new IllegalArgumentException("올바르지 않은 토픽 이름입니다.");
        }

        this.id = id;
        this.topicName = topicName;
    }

}
