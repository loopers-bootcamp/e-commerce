package com.loopers.application.order;

import com.loopers.domain.attribute.TimeRange;
import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponCommand;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.coupon.UserCoupon;
import com.loopers.domain.coupon.attribute.DiscountPolicy;
import com.loopers.domain.coupon.attribute.DiscountRule;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.attribute.OrderStatus;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointCommand;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.*;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import com.loopers.domain.user.attribute.Email;
import com.loopers.domain.user.attribute.Gender;
import com.loopers.utils.DatabaseCleanUp;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@SpringBootTest
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class OrderFacadeIntegrationTest {

    @InjectMocks
    private final OrderFacade sut;

    @MockitoSpyBean
    private final OrderService orderService;
    @MockitoSpyBean
    private final UserService userService;
    @MockitoSpyBean
    private final ProductService productService;
    @MockitoSpyBean
    private final PointService pointService;
    @MockitoSpyBean
    private final CouponService couponService;

    private final TransactionTemplate transactionTemplate;
    private final EntityManager entityManager;
    private final DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("주문을 생성할 때:")
    @Nested
    class Create {

        private User user;
        private Point point;
        private Product product;
        private ProductOption option1;
        private ProductOption option2;
        private ProductStock stock1;
        private ProductStock stock2;

        @BeforeEach
        void setUp() {
            user = User.builder()
                    .name("gildong")
                    .gender(Gender.MALE)
                    .birthDate(LocalDate.of(1990, 1, 1))
                    .email(new Email("gildong.hong@example.com"))
                    .build();
            transactionTemplate.executeWithoutResult(status -> entityManager.persist(user));

            point = Point.builder()
                    .balance(500_000L)
                    .userId(user.getId())
                    .build();
            transactionTemplate.executeWithoutResult(status -> entityManager.persist(point));

            product = Product.builder()
                    .name("Nike Shoes 2025")
                    .basePrice(120_000)
                    .build();
            transactionTemplate.executeWithoutResult(status -> entityManager.persist(product));

            option1 = ProductOption.builder()
                    .name("260")
                    .additionalPrice(0)
                    .productId(product.getId())
                    .build();
            option2 = ProductOption.builder()
                    .name("270")
                    .additionalPrice(1000)
                    .productId(product.getId())
                    .build();
            transactionTemplate.executeWithoutResult(status -> {
                entityManager.persist(option1);
                entityManager.persist(option2);
            });

            stock1 = ProductStock.builder()
                    .quantity(100)
                    .productOptionId(option1.getId())
                    .build();
            stock2 = ProductStock.builder()
                    .quantity(50)
                    .productOptionId(option2.getId())
                    .build();
            transactionTemplate.executeWithoutResult(status -> {
                entityManager.persist(stock1);
                entityManager.persist(stock2);
            });
        }

        @DisplayName("""
                회원 정보, 주문 상품, 재고, 포인트가 모두 유효하고 쿠폰을 사용하지 않으면,
                재고, 쿠폰, 포인트를 차감하지 않고 주문서를 생성한다.
                """)
        @Test
        void createOrderAndDeductNothing_withoutCoupon_whenValuesAndStatesIsValid() {
            // given
            List<OrderInput.Create.Product> cartItems = List.of(
                    OrderInput.Create.Product.builder().productOptionId(option1.getId()).quantity(2).build(),
                    OrderInput.Create.Product.builder().productOptionId(option2.getId()).quantity(1).build()
            );
            OrderInput.Create input = OrderInput.Create.builder()
                    .userName(user.getName())
                    .products(cartItems)
                    .build();

            // when
            OrderOutput.Create output = sut.create(input);

            // then
            assertThat(output).isNotNull();
            assertThat(output.getOrderId()).isNotNull();
            assertThat(output.getStatus()).isEqualTo(OrderStatus.CREATED);
            assertThat(output.getTotalPrice()).isEqualTo(361_000);
            assertThat(output.getDiscountAmount()).isZero();

            verify(userService, times(1)).getUser(user.getName());
            verify(productService, times(1)).getProductOptions(anyList());
            verify(couponService, times(1)).getDiscountAmount(any(CouponCommand.GetDiscountAmount.class));
            verify(pointService, times(1)).getPoint(user.getId());
            verify(orderService, times(1)).create(any(OrderCommand.Create.class));

            verify(productService, never()).deductStocks(any(ProductCommand.DeductStocks.class));
            verify(couponService, never()).use(any(CouponCommand.Use.class));
            verify(pointService, never()).spend(any(PointCommand.Spend.class));

            Order savedOrder = entityManager.find(Order.class, output.getOrderId());
            assertThat(savedOrder).isNotNull();
            assertThat(savedOrder.getId()).isEqualTo(output.getOrderId());
            assertThat(savedOrder.getTotalPrice()).isEqualTo(output.getTotalPrice());
            assertThat(savedOrder.getDiscountAmount()).isZero();
            assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.CREATED);
            assertThat(savedOrder.getUserId()).isEqualTo(user.getId());
        }

        @DisplayName("""
                회원 정보, 주문 상품, 재고, 포인트가 모두 유효하고 쿠폰을 사용하면,
                재고, 쿠폰, 포인트를 차감하지 않고 주문서를 생성한다.
                """)
        @Test
        void createOrderAndDeductNothing_withCoupon_whenValuesAndStatesIsValid() {
            // given
            Coupon coupon1 = Coupon.builder()
                    .name("Shopping Mall 1st Anniversary")
                    .discountPolicy(DiscountPolicy.builder()
                            .discountRule(DiscountRule.FIXED_AMOUNT)
                            .discountValue(BigDecimal.valueOf(10_000))
                            .build()
                    )
                    .validityPeriod(Period.ofMonths(1))
                    .issuedRange(TimeRange.of(
                            ZonedDateTime.now().minusDays(1),
                            ZonedDateTime.now().plusDays(27)
                    ))
                    .build();
            Coupon coupon2 = Coupon.builder()
                    .name("Happy Birthday!")
                    .discountPolicy(DiscountPolicy.builder()
                            .discountRule(DiscountRule.FIXED_RATE)
                            .discountValue(BigDecimal.valueOf(0.1))
                            .maxDiscountAmount(30_000)
                            .build()
                    )
                    .validityPeriod(Period.ofMonths(1))
                    .issuedRange(TimeRange.WHENEVER)
                    .build();
            transactionTemplate.executeWithoutResult(status ->
                    Stream.of(coupon1, coupon2).forEach(entityManager::persist));

            UserCoupon userCoupon1 = UserCoupon.builder()
                    .validRange(TimeRange.of(coupon1.getValidityPeriod()))
                    .userId(user.getId())
                    .couponId(coupon1.getId())
                    .build();
            UserCoupon userCoupon2 = UserCoupon.builder()
                    .validRange(TimeRange.of(coupon2.getValidityPeriod()))
                    .userId(user.getId())
                    .couponId(coupon2.getId())
                    .build();
            transactionTemplate.executeWithoutResult(status ->
                    Stream.of(userCoupon1, userCoupon2).forEach(entityManager::persist));

            List<OrderInput.Create.Product> cartItems = List.of(
                    OrderInput.Create.Product.builder().productOptionId(option1.getId()).quantity(2).build(),
                    OrderInput.Create.Product.builder().productOptionId(option2.getId()).quantity(1).build()
            );
            List<Long> userCouponIds = List.of(userCoupon1.getId(), userCoupon2.getId());

            OrderInput.Create input = OrderInput.Create.builder()
                    .userName(user.getName())
                    .products(cartItems)
                    .userCouponIds(userCouponIds)
                    .build();

            // when
            OrderOutput.Create output = sut.create(input);

            // then
            assertThat(output).isNotNull();
            assertThat(output.getOrderId()).isNotNull();
            assertThat(output.getStatus()).isEqualTo(OrderStatus.CREATED);
            assertThat(output.getTotalPrice()).isEqualTo(361_000);
            assertThat(output.getDiscountAmount()).isEqualTo(10_000);

            verify(userService, times(1)).getUser(user.getName());
            verify(productService, times(1)).getProductOptions(anyList());
            verify(couponService, times(1)).getDiscountAmount(any(CouponCommand.GetDiscountAmount.class));
            verify(pointService, times(1)).getPoint(user.getId());
            verify(orderService, times(1)).create(any(OrderCommand.Create.class));

            verify(productService, never()).deductStocks(any(ProductCommand.DeductStocks.class));
            verify(couponService, never()).use(any(CouponCommand.Use.class));
            verify(pointService, never()).spend(any(PointCommand.Spend.class));

            Order savedOrder = entityManager.find(Order.class, output.getOrderId());
            assertThat(savedOrder).isNotNull();
            assertThat(savedOrder.getId()).isEqualTo(output.getOrderId());
            assertThat(savedOrder.getTotalPrice()).isEqualTo(output.getTotalPrice());
            assertThat(savedOrder.getDiscountAmount()).isEqualTo(10_000);
            assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.CREATED);
            assertThat(savedOrder.getUserId()).isEqualTo(user.getId());
        }

    }

}
