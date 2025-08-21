package com.loopers.infrastructure.payment.client;

import com.loopers.domain.payment.attribute.CardNumber;
import com.loopers.domain.payment.attribute.CardType;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.HttpServerErrorException;

import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest
class PgSimulatorTest {

    private static final String CB_REQUEST_TRANSACTION = "cb--payment-gateway--request-transaction";
    private static final String CB_GET_TRANSACTION = "cb--payment-gateway--get-transaction";

    @Autowired
    private PgSimulator sut;

    @MockitoBean
    private PgSimulatorClient client;
    @Autowired
    protected CircuitBreakerRegistry registry;

    @DisplayName("minimum-number-of-calls을 초과하지 않으면 서킷브레이커가 열리지 않는다.")
    @Test
    void doNotOpen() {
        // given
        UUID orderId = UUID.randomUUID();
        CardType cardType = CardType.KB;
        CardNumber cardNumber = new CardNumber("0000000000000000");
        Long amount = 5000L;

        CircuitBreaker circuitBreaker = registry.circuitBreaker(CB_REQUEST_TRANSACTION);
        circuitBreaker.reset();
        circuitBreaker.transitionToClosedState();

        int minCallCount = circuitBreaker.getCircuitBreakerConfig().getMinimumNumberOfCalls();
        given(client.requestTransaction(any(), any()))
                .willThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        System.out.println("minCallCount:" + minCallCount);

        // when
        IntStream.range(0, minCallCount).forEach(i -> {
            System.out.println("state: " + circuitBreaker.getState());
            circuitBreaker.executeRunnable(() -> {
                throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
            });
//            sut.requestTransaction(orderId, cardType, cardNumber, amount);
        });

        // then
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

}
