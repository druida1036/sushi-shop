package com.livebarn.sushishop.mappers;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.livebarn.sushishop.models.OrderCreationDto;
import com.livebarn.sushishop.models.OrderCreationResponse;
import com.livebarn.sushishop.models.OrderStatusDto;
import com.livebarn.sushishop.models.OrderStatusResponse;
import com.livebarn.sushishop.models.Response;
import org.hamcrest.collection.IsMapContaining;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ResponseMapperTest {

    @Test
    void toSuccessRecordsFoundResponse() {
        Map<String, List<OrderStatusDto>> orders = Collections.singletonMap("created",
            Collections.singletonList(OrderStatusDto.builder().orderId(1).timeSpent(40).build()));
        Response<OrderStatusResponse> orderStatusResponseResponse = ResponseMapper.toSuccessRecordsFoundResponse(
            orders);

        assertAll(
            () -> assertEquals(0, orderStatusResponseResponse.getCode()),
            () -> assertThat(orderStatusResponseResponse.getBody().getOrders(), IsMapContaining.hasEntry("created",
                Collections.singletonList(OrderStatusDto.builder().orderId(1).timeSpent(40).build())))
        );
    }

    @Test
    void toSuccessSubmittedOrderResponse() {
        OrderCreationDto orderCreationDto = OrderCreationDto.builder().build();
        Response<OrderCreationResponse> orderCreationResponseResponse = ResponseMapper.toSuccessSubmittedOrderResponse(
            orderCreationDto);

        assertEquals(orderCreationDto, orderCreationResponseResponse.getBody().getOrder());

    }

    @Test
    void toSuccessPausedOrderResponse() {
        Response<?> response = ResponseMapper.toSuccessPausedOrderResponse();
        assertAll(
            () -> assertEquals(0, response.getCode()),
            () -> assertEquals("Order paused", response.getMsg()),
            () -> assertNull(response.getBody())
        );
    }

    @Test
    void toSuccessCancelledOrderResponse() {
        Response<?> response = ResponseMapper.toSuccessCancelledOrderResponse();
        assertAll(
            () -> assertEquals(0, response.getCode()),
            () -> assertEquals("Order cancelled", response.getMsg()),
            () -> assertNull(response.getBody())
        );
    }

    @Test
    void toSuccessResumedOrderResponse() {
        Response<?> response = ResponseMapper.toSuccessResumedOrderResponse();
        assertAll(
            () -> assertEquals(0, response.getCode()),
            () -> assertEquals("Order resumed", response.getMsg()),
            () -> assertNull(response.getBody())
        );
    }

    @Test
    void toErrorResponse() {
        Response<?> response = ResponseMapper.toErrorResponse(1, "test error");
        assertAll(
            () -> assertEquals(1, response.getCode()),
            () -> assertEquals("test error", response.getMsg()),
            () -> assertNull(response.getBody())
        );
    }
}