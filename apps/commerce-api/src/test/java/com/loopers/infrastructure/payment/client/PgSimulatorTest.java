package com.loopers.infrastructure.payment.client;

import com.loopers.domain.payment.PaymentGateway;
import com.loopers.domain.payment.attribute.CardNumber;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.springboot3.retry.autoconfigure.RetryAutoConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.HttpServerErrorException;

import static com.loopers.resilience4j.test.CircuitBreakerAssertion.assertThatCircuitBreaker;
import static org.instancio.Select.root;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@Slf4j
class PgSimulatorTest {

    private static final String TRANSACT = "payment-gateway--transact";
    private static final String GET_TRANSACTIONS = "payment-gateway--get-transactions";

    @DisplayName("서킷브레이커를 사용할 때 (excluding retry):")
    @ImportAutoConfiguration(exclude = RetryAutoConfiguration.class)
    @SpringBootTest
    @Nested
    class UseCircuitBreaker {

        @Autowired
        private PgSimulator sut;

        @MockitoBean
        private PgSimulatorClient client;

        @Autowired
        private CircuitBreakerRegistry registry;

        @DisplayName("최소 요청 수보다 적으면, 서킷브레이커가 열리지 않는다.")
        @Test
        void circuitBreakerWillNotOpen_ifThereAreFewerThanMinimumNumberOfCalls() {
            // given
            PaymentGateway.Request.Transact request = mock(PaymentGateway.Request.Transact.class);
            given(request.cardNumber()).willReturn(new CardNumber("0000000000000000"));
            given(client.transact(anyString(), any())).willThrow(HttpServerErrorException.class);

            CircuitBreaker circuitBreaker = registry.circuitBreaker(TRANSACT);
            CircuitBreakerConfig config = circuitBreaker.getCircuitBreakerConfig();
            Integer randomCallCount = Instancio.of(Integer.class)
                    .generate(root(), gen -> gen.ints().range(1, config.getMinimumNumberOfCalls() - 1))
                    .create();

            // when & then
            assertThatCircuitBreaker()
                    .withCircuitBreaker(circuitBreaker)
                    .withInitialState(CircuitBreaker.State.CLOSED)
                    .withCallCount(randomCallCount)
                    .isExecutedBy(() -> sut.transact(request))
                    .isDone()
                    .isClosed()
                    .hasNoSuccessCall();

            verify(client, times(randomCallCount)).transact(anyString(), any());
        }

        @DisplayName("요청 수가 슬라이딩 윈도우 크기보다 적어도, 최소 요청 수를 만족하면, 서킷브레이커가 열린다.")
        @Test
        void circuitBreakerOpens_whenMinimumNumberOfCallsIsMet_evenIfNumberOfRequestsIsLessThanSlidingWindowSize() {
            // given
            PaymentGateway.Request.Transact request = mock(PaymentGateway.Request.Transact.class);
            given(request.cardNumber()).willReturn(new CardNumber("0000000000000000"));
            given(client.transact(anyString(), any())).willThrow(HttpServerErrorException.class);

            CircuitBreaker circuitBreaker = registry.circuitBreaker(TRANSACT);
            CircuitBreakerConfig config = circuitBreaker.getCircuitBreakerConfig();
            int minimumNumberOfCalls = config.getMinimumNumberOfCalls();

            Integer randomCallCount = Instancio.of(Integer.class)
                    .generate(root(), gen -> gen.ints().range(minimumNumberOfCalls, config.getSlidingWindowSize() - 1))
                    .create();

            // when & then
            assertThatCircuitBreaker()
                    .withCircuitBreaker(circuitBreaker)
                    .withInitialState(CircuitBreaker.State.CLOSED)
                    .withCallCount(randomCallCount)
                    .isExecutedBy(() -> sut.transact(request))
                    .isDone()
                    .isOpen()
                    .hasNoSuccessCall();

            verify(client, times(minimumNumberOfCalls)).transact(anyString(), any());
        }

    }

}
