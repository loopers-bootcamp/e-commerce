package com.loopers.application.user;

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

    @Inboxing
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void createUserPoint(UserEvent.Join event) {
        // 포인트 충전 시 Point를 생성하면 PointRepository가 User를 반환해야 한다.
        // 상호의존성을 application 레이어에서 해결하고자, 회원가입과 동시에 생성한다.
        pointService.create(event.userId());
    }

}
