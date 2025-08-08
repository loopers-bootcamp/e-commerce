package com.loopers.domain.order;

import com.loopers.domain.order.attribute.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@MockitoSettings
class OrderServiceTest {

    @InjectMocks
    private OrderService sut;

    @Mock
    private OrderRepository orderRepository;

    @DisplayName("주문을 생성할 때:")
    @Nested
    class Create {

        @DisplayName("주어진 값이 올바르면, CREATED 상태의 주문을 반환한다.")
        @Test
        void createNewOrder_withValidValues() {
            // given
            Long userId = 1L;
            Long totalPrice = 150_000L;
            UUID orderId = UUID.fromString("00000000-0000-1000-8000-000000000000");

            given(orderRepository.findNextOrderId()).willReturn(orderId);

            List<OrderCommand.Create.Product> products = List.of(
                    OrderCommand.Create.Product.builder().productOptionId(1L).quantity(3).price(30_000).build(),
                    OrderCommand.Create.Product.builder().productOptionId(2L).quantity(1).price(60_000).build()
            );

            OrderCommand.Create command = OrderCommand.Create.builder()
                    .userId(userId)
                    .totalPrice(totalPrice)
                    .discountAmount(0)
                    .products(products)
                    .build();

            // when
            OrderResult.Create result = sut.create(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getOrderId()).isEqualTo(orderId);
            assertThat(result.getTotalPrice()).isEqualTo(totalPrice);
            assertThat(result.getStatus()).isEqualTo(OrderStatus.CREATED);
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getProducts()).hasSameSizeAs(products);

            verify(orderRepository, times(1)).findNextOrderId();
            verify(orderRepository, times(1)).save(any(Order.class));
        }

    }

}
