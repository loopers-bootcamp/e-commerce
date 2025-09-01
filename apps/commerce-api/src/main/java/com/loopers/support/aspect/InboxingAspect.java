package com.loopers.support.aspect;

import com.loopers.domain.saga.SagaCommand;
import com.loopers.domain.saga.SagaResult;
import com.loopers.domain.saga.SagaService;
import com.loopers.domain.saga.event.SagaEvent;
import com.loopers.support.annotation.Inboxing;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class InboxingAspect {

    private final SagaService sagaService;

    @Qualifier(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME)
    private final TaskExecutor taskExecutor;

    @Around(value = "inboxingPointcut(inboxing, event)", argNames = "pjp, inboxing, event")
    public Object handleAdvice(ProceedingJoinPoint pjp, Inboxing inboxing, SagaEvent event) throws Throwable {
        SagaCommand.Inbound command = new SagaCommand.Inbound(
                event.eventKey(),
                event.eventName(),
                event
        );

        // 비동기와 멱등성 보장은 공존할 수 없다.
        if (inboxing.async() && !inboxing.idempotent()) {
            taskExecutor.execute(() -> sagaService.inbound(command));
            return pjp.proceed();
        }

        SagaResult.Inbound inbound = sagaService.inbound(command);

        // 멱등적 연산을 위해, 이미 저장된 이벤트라면 메인 로직을 실행하지 않는다.
        if (inboxing.idempotent() && !inbound.saved()) {
            // 반환 타입이 void인 메서드만 위빙하므로, null을 반환해도 문제없다.
            return null;
        }

        return pjp.proceed();
    }

    @Pointcut("@annotation(inboxing) && execution(void *..*.*(..)) && args(event)")
    private void inboxingPointcut(Inboxing inboxing, SagaEvent event) {
    }

}
