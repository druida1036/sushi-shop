package com.livebarn.sushishop.mappers;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.livebarn.sushishop.models.OrderCreationDto;
import com.livebarn.sushishop.models.OrderCreationResponse;
import com.livebarn.sushishop.models.OrderStatusDto;
import com.livebarn.sushishop.models.OrderStatusResponse;
import org.hamcrest.collection.IsMapContaining;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderResponseMapperTest {

    @Test
    void toOrderStatusResponse() {
        Map<String, List<OrderStatusDto>> orders = Collections.singletonMap("created",
            Collections.singletonList(OrderStatusDto.builder().orderId(1).timeSpent(0).build()));

        OrderStatusResponse orderStatusResponse = OrderResponseMapper.toOrderStatusResponse(orders);

        assertAll(
            () -> assertThat(orderStatusResponse.getOrders(), IsMapContaining.hasEntry("created",
                Collections.singletonList(OrderStatusDto.builder().orderId(1).timeSpent(0).build())))
        );
    }

    @Test
    void toOrderCreationResponse() {
        OrderCreationDto orderCreationDto = OrderCreationDto.builder().build();
        OrderCreationResponse orderCreationResponse = OrderResponseMapper.toOrderCreationResponse(
            orderCreationDto);
        assertEquals(orderCreationDto, orderCreationResponse.getOrder());
    }
}