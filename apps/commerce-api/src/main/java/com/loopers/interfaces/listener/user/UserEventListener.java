package com.loopers.interfaces.listener.user;

import com.loopers.domain.point.PointService;
import com.loopers.domain.user.event.UserEvent;
import com.loopers.support.annotation.Inboxing;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class UserEventListener {

    private final PointService pointService;

    /**
     * {@link TransactionPhase#BEFORE_COMMIT}: 포인트 엔터티를 지연 생성 시,
     * 없는 레코드에 락을 걸 수 없기 때문에 Lost Update 문제가 발생할 수 있다.
     * 따라서 같은 트랜잭션에 참여한다.
     */
    @Inboxing(idempotent = true)
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void createUserPoint(UserEvent.Join event) {
        pointService.create(event.userId());
    }

}
