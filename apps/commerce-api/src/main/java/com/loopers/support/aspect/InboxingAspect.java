package com.loopers.support.aspect;

import com.loopers.domain.saga.SagaCommand;
import com.loopers.domain.saga.SagaService;
import com.loopers.domain.saga.event.SagaEvent;
import com.loopers.support.annotation.Inboxing;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class InboxingAspect {

    private final SagaService sagaService;

    @Before(value = "inboxingPointcut(inboxing, event)", argNames = "joinPoint, inboxing, event")
    public void handleAdvice(JoinPoint joinPoint, Inboxing inboxing, SagaEvent event) {
        SagaCommand.Inbound command = new SagaCommand.Inbound(
                event.eventKey(),
                event.eventName(),
                event
        );
        sagaService.inbound(command);
    }

    @Pointcut("@annotation(inboxing) && args(event)")
    private void inboxingPointcut(Inboxing inboxing, SagaEvent event) {
    }

}
