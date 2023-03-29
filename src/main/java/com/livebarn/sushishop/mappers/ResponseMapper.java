package com.livebarn.sushishop.mappers;

import java.util.List;
import java.util.Map;

import com.livebarn.sushishop.models.OrderCreationDto;
import com.livebarn.sushishop.models.OrderCreationResponse;
import com.livebarn.sushishop.models.OrderStatusDto;
import com.livebarn.sushishop.models.OrderStatusResponse;
import com.livebarn.sushishop.models.Response;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ResponseMapper {

    private static final String ORDERS_FOUND = "Orders found";
    private static final String ORDER_CREATED = "Order created";
    private static final String ORDER_PAUSED = "Order paused";
    private static final String ORDER_CANCELLED = "Order cancelled";
    private static final String ORDER_RESUMED = "Order resumed";

    public static Response<OrderStatusResponse> toSuccessRecordsFoundResponse(
        Map<String, List<OrderStatusDto>> orders) {
        return createSuccessResponse(ORDERS_FOUND, OrderResponseMapper.toOrderStatusResponse(orders));
    }

    public static Response<OrderCreationResponse> toSuccessSubmittedOrderResponse(OrderCreationDto orderCreationDto) {
        return createSuccessResponse(ORDER_CREATED, OrderResponseMapper.toOrderCreationResponse(orderCreationDto));
    }

    public static Response<?> toSuccessPausedOrderResponse() {
        return createSuccessResponse(ORDER_PAUSED);
    }

    public static Response<?> toSuccessCancelledOrderResponse() {
        return createSuccessResponse(ORDER_CANCELLED);
    }

    public static Response<?> toSuccessResumedOrderResponse() {
        return createSuccessResponse(ORDER_RESUMED);
    }

    public static Response<?> toErrorResponse(int code, String msg) {
        return new Response<>(code, msg);
    }

    private <T> Response<T> createSuccessResponse(String msg, T body) {
        return Response.<T>builder().code(0).msg(msg).body(body).build();
    }

    private Response<?> createSuccessResponse(String msg) {
        return Response.builder().code(0).msg(msg).build();
    }

}
