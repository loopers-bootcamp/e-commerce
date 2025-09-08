package com.loopers.domain.activity.event;

public interface ActivityEventPublisher {

    void publishEvent(ActivityEvent.Like event);

    void publishEvent(ActivityEvent.Dislike event);

    void publishEvent(ActivityEvent.View event);

}
