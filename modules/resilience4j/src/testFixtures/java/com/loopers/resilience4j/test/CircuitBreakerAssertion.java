package com.loopers.resilience4j.test;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerOnStateTransitionEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CircuitBreakerAssertion {

    private final AtomicInteger successEvents = new AtomicInteger();
    private final AtomicInteger errorEvents = new AtomicInteger();
    private final AtomicInteger notPermittedEvents = new AtomicInteger();
    private final List<CircuitBreakerOnStateTransitionEvent> transitions = new CopyOnWriteArrayList<>();

    private final CircuitBreaker circuitBreaker;
    private final CircuitBreaker.State initialState;
    private final int callCount;

    public static Initializer assertThatCircuitBreaker() {
        return new Initializer();
    }

    public CircuitBreakerAssertion isDone() {
        int totalCallCount = this.successEvents.get() + this.errorEvents.get() + this.notPermittedEvents.get();
        assertThat(totalCallCount).isEqualTo(this.callCount);
        return this;
    }

    public CircuitBreakerAssertion hasNoSuccessCall() {
        assertThat(this.successEvents).hasValue(0);
        assertThat(this.errorEvents.get() + this.notPermittedEvents.get()).isEqualTo(this.callCount);
        return this;
    }

    public CircuitBreakerAssertion isOpen() {
        assertThat(this.circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
        return this;
    }

    public CircuitBreakerAssertion isClosed() {
        assertThat(this.circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
        return this;
    }

    private CircuitBreakerAssertion execute(Runnable runnable) {
        this.circuitBreaker.reset();

        switch (this.initialState) {
            case OPEN -> this.circuitBreaker.transitionToOpenState();
            case CLOSED -> this.circuitBreaker.transitionToClosedState();
            case HALF_OPEN -> this.circuitBreaker.transitionToHalfOpenState();
            case DISABLED -> this.circuitBreaker.transitionToDisabledState();
            case METRICS_ONLY -> this.circuitBreaker.transitionToMetricsOnlyState();
            case FORCED_OPEN -> this.circuitBreaker.transitionToForcedOpenState();
        }

        this.circuitBreaker.getEventPublisher()
                .onSuccess(e -> this.successEvents.incrementAndGet())
                .onError(e -> this.errorEvents.incrementAndGet())
                .onCallNotPermitted(e -> this.notPermittedEvents.incrementAndGet())
                .onStateTransition(this.transitions::add);

        IntStream.range(0, this.callCount)
                .forEach(i -> assertThatException().isThrownBy(runnable::run));
        return this;
    }

    // -------------------------------------------------------------------------------------------------

    public static class Initializer {

        private CircuitBreaker circuitBreaker;
        private CircuitBreaker.State initialState = CircuitBreaker.State.CLOSED;
        private int callCount;

        public Initializer withCircuitBreaker(CircuitBreaker circuitBreaker) {
            this.circuitBreaker = Objects.requireNonNull(circuitBreaker);
            return this;
        }

        public Initializer withInitialState(CircuitBreaker.State initialState) {
            this.initialState = Objects.requireNonNull(initialState);
            return this;
        }

        public Initializer withCallCount(int callCount) {
            this.callCount = callCount;
            return this;
        }

        public CircuitBreakerAssertion isExecutedBy(Runnable runnable) {
            CircuitBreakerAssertion assertion = new CircuitBreakerAssertion(
                    this.circuitBreaker,
                    this.initialState,
                    this.callCount
            );
            return assertion.execute(Objects.requireNonNull(runnable));
        }

    }

}
