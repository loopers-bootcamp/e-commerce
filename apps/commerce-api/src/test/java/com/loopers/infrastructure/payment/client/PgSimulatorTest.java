package com.loopers.infrastructure.payment.client;

import com.loopers.domain.payment.PaymentGateway;
import com.loopers.domain.payment.attribute.CardNumber;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.springboot3.circuitbreaker.autoconfigure.CircuitBreakerAutoConfiguration;
import io.github.resilience4j.springboot3.retry.autoconfigure.RetryAutoConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.HttpServerErrorException;

import java.util.stream.IntStream;

import static com.loopers.resilience4j.test.CircuitBreakerAssertion.assertThatCircuitBreaker;
import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.root;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@Slf4j
class PgSimulatorTest {

    private static final String REQUEST_TRANSACTION = "payment-gateway--request-transaction";
    private static final String GET_TRANSACTION = "payment-gateway--get-transaction";

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
            given(client.transact(any(), any())).willThrow(HttpServerErrorException.class);

            CircuitBreaker circuitBreaker = registry.circuitBreaker(REQUEST_TRANSACTION);
            CircuitBreakerConfig config = circuitBreaker.getCircuitBreakerConfig();
            Integer randomCallCount = Instancio.of(Integer.class)
                    .generate(root(), gen -> gen.ints().range(1, config.getMinimumNumberOfCalls() - 1))
                    .create();

            // when & then
            assertThatCircuitBreaker()
                    .withCircuitBreaker(circuitBreaker)
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
            given(client.transact(any(), any())).willThrow(HttpServerErrorException.class);

            CircuitBreaker circuitBreaker = registry.circuitBreaker(REQUEST_TRANSACTION);
            CircuitBreakerConfig config = circuitBreaker.getCircuitBreakerConfig();
            int minimumNumberOfCalls = config.getMinimumNumberOfCalls();

            Integer randomCallCount = Instancio.of(Integer.class)
                    .generate(root(), gen -> gen.ints().range(minimumNumberOfCalls, config.getSlidingWindowSize() - 1))
                    .create();

            // when & then
            assertThatCircuitBreaker()
                    .withCircuitBreaker(circuitBreaker)
                    .withCallCount(randomCallCount)
                    .isExecutedBy(() -> sut.transact(request))
                    .isDone()
                    .isOpen()
                    .hasNoSuccessCall();

            verify(client, times(minimumNumberOfCalls)).transact(anyString(), any());
        }

    }

    // -------------------------------------------------------------------------------------------------

    @DisplayName("재시도를 사용할 때 (excluding circuit breaker):")
    @ImportAutoConfiguration(exclude = CircuitBreakerAutoConfiguration.class)
    @SpringBootTest
    @Nested
    class UseRetry {

        @Autowired
        private PgSimulator sut;

        @MockitoBean
        private PgSimulatorClient client;

        @Autowired
        private RetryRegistry registry;

        @DisplayName("최대 시도 횟수 내에서 호출했는지 확인한다.")
        @RepeatedTest(10)
        void verifyThatCallWasMadeWithinMaxAttempts() {
            // given
            PaymentGateway.Request.Transact request = mock(PaymentGateway.Request.Transact.class);
            given(request.cardNumber()).willReturn(new CardNumber("0000000000000000"));
            given(client.transact(any(), any())).willThrow(HttpServerErrorException.class);

            Retry retry = registry.retry(REQUEST_TRANSACTION);
            int maxAttempts = retry.getRetryConfig().getMaxAttempts();
            Integer randomCallCount = Instancio.of(Integer.class)
                    .generate(root(), gen -> gen.ints().range(1, maxAttempts * 10))
                    .create();

            // when
            IntStream.range(0, randomCallCount)
                    .forEach(i -> sut.transact(request));

            // then
            int numberOfTotalCalls = (int) retry.getMetrics().getNumberOfTotalCalls();
            assertThat(numberOfTotalCalls).isLessThanOrEqualTo(maxAttempts);
            verify(client, times(numberOfTotalCalls)).transact(anyString(), any());
        }

    }

}
