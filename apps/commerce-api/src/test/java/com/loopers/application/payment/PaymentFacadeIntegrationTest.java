package com.loopers.application.payment;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderProduct;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.payment.attribute.PaymentMethod;
import com.loopers.domain.payment.attribute.PaymentStatus;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointCommand;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.Stock;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import com.loopers.domain.user.attribute.Email;
import com.loopers.domain.user.attribute.Gender;
import com.loopers.utils.DatabaseCleanUp;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class PaymentFacadeIntegrationTest {

    private final PaymentFacade sut;

    @MockitoSpyBean
    private final PaymentService paymentService;
    @MockitoSpyBean
    private final OrderService orderService;
    @MockitoSpyBean
    private final UserService userService;
    @MockitoSpyBean
    private final ProductService productService;
    @MockitoSpyBean
    private final PointService pointService;

    private final TransactionTemplate transactionTemplate;
    private final EntityManager entityManager;
    private final DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("주문 건을 결제할 때:")
    @Nested
    class Pay {

        @DisplayName("결제 금액이 0원이면, 포인트 차감 없이 결제가 완료된다.")
        @Test
        void completePayment_withoutPointDeduction_whenAmountIsZero() {
            // given
            User user = User.builder()
                    .name("gildong")
                    .gender(Gender.MALE)
                    .birthDate(LocalDate.of(1990, 1, 1))
                    .email(new Email("gildong.hong@example.com"))
                    .build();
            transactionTemplate.executeWithoutResult(status -> entityManager.persist(user));

            Point point = Point.builder()
                    .balance(0L)
                    .userId(user.getId())
                    .build();
            transactionTemplate.executeWithoutResult(status -> entityManager.persist(point));

            Stock stock = Stock.builder()
                    .quantity(100)
                    .productOptionId(1L)
                    .build();
            transactionTemplate.executeWithoutResult(status -> entityManager.persist(stock));

            Order order = Order.builder()
                    .id(UUID.fromString("00000000-0000-1000-8000-000000000000"))
                    .totalPrice(0L)
                    .userId(user.getId())
                    .build();
            OrderProduct orderProduct = OrderProduct.builder()
                    .price(0)
                    .quantity(1)
                    .orderId(order.getId())
                    .productOptionId(1L)
                    .build();
            transactionTemplate.executeWithoutResult(status -> {
                entityManager.persist(order);
                entityManager.persist(orderProduct);
            });

            PaymentInput.Pay input = PaymentInput.Pay.builder()
                    .userName(user.getName())
                    .orderId(order.getId())
                    .paymentMethod(Instancio.create(PaymentMethod.class))
                    .build();

            // when
            PaymentOutput.Pay output = sut.pay(input);

            // then
            assertThat(output).isNotNull();
            assertThat(output.getPaymentId()).isNotNull();
            assertThat(output.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETE);

            verify(userService, times(1)).getUser(user.getName());
            verify(orderService, times(1)).getOrderDetail(order.getId());
            verify(productService, times(1)).deductStocks(any(ProductCommand.DeductStocks.class));
            verify(pointService, never()).spend(any(PointCommand.Spend.class));
            verify(paymentService, times(1)).pay(any(PaymentCommand.Pay.class));
            verify(orderService, times(1)).complete(order.getId());

            Payment savedPayment = entityManager.find(Payment.class, output.getPaymentId());
            assertThat(savedPayment).isNotNull();
            assertThat(savedPayment.getId()).isEqualTo(output.getPaymentId());
            assertThat(savedPayment.getAmount()).isEqualTo(order.getTotalPrice());
            assertThat(savedPayment.getStatus()).isEqualTo(output.getPaymentStatus());
            assertThat(savedPayment.getMethod()).isEqualTo(input.getPaymentMethod());
            assertThat(savedPayment.getUserId()).isEqualTo(user.getId());
            assertThat(savedPayment.getOrderId()).isEqualTo(order.getId());
        }

        @DisplayName("결제 금액이 0보다 크면, 포인트를 차감하고 결제가 완료된다.")
        @Test
        void completePayment_withDeduction_whenAmountIsPositive() {
            // given
            User user = User.builder()
                    .name("gildong")
                    .gender(Gender.MALE)
                    .birthDate(LocalDate.of(1990, 1, 1))
                    .email(new Email("gildong.hong@example.com"))
                    .build();
            transactionTemplate.executeWithoutResult(status -> entityManager.persist(user));

            Point point = Point.builder()
                    .balance(500_000L)
                    .userId(user.getId())
                    .build();
            transactionTemplate.executeWithoutResult(status -> entityManager.persist(point));

            Stock stock1 = Stock.builder()
                    .quantity(100)
                    .productOptionId(1L)
                    .build();
            Stock stock2 = Stock.builder()
                    .quantity(50)
                    .productOptionId(2L)
                    .build();
            transactionTemplate.executeWithoutResult(status -> {
                entityManager.persist(stock1);
                entityManager.persist(stock2);
            });

            Order order = Order.builder()
                    .id(UUID.fromString("00000000-0000-1000-8000-000000000000"))
                    .totalPrice(139_000L)
                    .userId(user.getId())
                    .build();
            OrderProduct orderProduct1 = OrderProduct.builder()
                    .price(120_000)
                    .quantity(2)
                    .orderId(order.getId())
                    .productOptionId(1L)
                    .build();
            OrderProduct orderProduct2 = OrderProduct.builder()
                    .price(121_000)
                    .quantity(1)
                    .orderId(order.getId())
                    .productOptionId(2L)
                    .build();
            transactionTemplate.executeWithoutResult(status -> {
                entityManager.persist(order);
                entityManager.persist(orderProduct1);
                entityManager.persist(orderProduct2);
            });

            PaymentInput.Pay input = PaymentInput.Pay.builder()
                    .userName(user.getName())
                    .orderId(order.getId())
                    .paymentMethod(Instancio.create(PaymentMethod.class))
                    .build();

            // when
            PaymentOutput.Pay output = sut.pay(input);

            // then
            assertThat(output).isNotNull();
            assertThat(output.getPaymentId()).isNotNull();
            assertThat(output.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETE);

            verify(userService, times(1)).getUser(user.getName());
            verify(orderService, times(1)).getOrderDetail(order.getId());
            verify(productService, times(1)).deductStocks(any(ProductCommand.DeductStocks.class));
            verify(pointService, times(1)).spend(any(PointCommand.Spend.class));
            verify(paymentService, times(1)).pay(any(PaymentCommand.Pay.class));
            verify(orderService, times(1)).complete(order.getId());

            Payment savedPayment = entityManager.find(Payment.class, output.getPaymentId());
            assertThat(savedPayment).isNotNull();
            assertThat(savedPayment.getId()).isEqualTo(output.getPaymentId());
            assertThat(savedPayment.getAmount()).isEqualTo(order.getTotalPrice());
            assertThat(savedPayment.getStatus()).isEqualTo(output.getPaymentStatus());
            assertThat(savedPayment.getMethod()).isEqualTo(input.getPaymentMethod());
            assertThat(savedPayment.getUserId()).isEqualTo(user.getId());
            assertThat(savedPayment.getOrderId()).isEqualTo(order.getId());
        }

    }

}
