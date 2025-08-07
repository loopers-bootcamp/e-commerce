package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderFacade;
import com.loopers.application.order.OrderInput;
import com.loopers.application.order.OrderOutput;
import com.loopers.interfaces.api.ApiHeader;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderV1Controller implements OrderV1ApiSpec {

    private final OrderFacade orderFacade;

    @GetMapping("/{orderId}")
    @Override
    public ApiResponse<OrderResponse.GetOrderDetail> getOrderDetail(
            @RequestHeader(ApiHeader.USER_ID)
            String userName,

            @PathVariable
            UUID orderId
    ) {
        OrderInput.GetOrderDetail input = OrderInput.GetOrderDetail.builder()
                .userName(userName)
                .orderId(orderId)
                .build();
        OrderOutput.GetOrderDetail order = orderFacade.getOrderDetail(input);
        OrderResponse.GetOrderDetail response = OrderResponse.GetOrderDetail.from(order);

        return ApiResponse.success(response);
    }

    @PostMapping
    @Override
    public ApiResponse<OrderResponse.Create> create(
            @RequestHeader(ApiHeader.USER_ID)
            String userName,

            @RequestBody
            OrderRequest.Create request
    ) {
        OrderInput.Create input = OrderInput.Create.builder()
                .userName(userName)
                .products(request.getProducts()
                        .stream()
                        .map(product -> OrderInput.Create.Product.builder()
                                .productOptionId(product.getProductOptionId())
                                .quantity(product.getQuantity())
                                .build()
                        )
                        .toList()
                )
                .userCouponIds(List.copyOf(request.getUserCouponIds()))
                .build();

        OrderOutput.Create output = orderFacade.create(input);
        OrderResponse.Create response = OrderResponse.Create.from(output);

        return ApiResponse.success(response);
    }

}
