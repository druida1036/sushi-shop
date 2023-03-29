package com.livebarn.sushishop.mappers;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.livebarn.sushishop.entities.OrderStatus;
import com.livebarn.sushishop.entities.Sushi;
import com.livebarn.sushishop.entities.SushiOrder;
import com.livebarn.sushishop.models.OrderCreationDto;
import com.livebarn.sushishop.models.OrderStatusDto;
import org.hamcrest.collection.IsMapContaining;
import org.junit.jupiter.api.Test;

import static com.livebarn.sushishop.mappers.OrderMapper.toOrderCreationDto;
import static com.livebarn.sushishop.mappers.OrderMapper.toOrderStatusDto;
import static com.livebarn.sushishop.mappers.OrderMapper.toOrderStatusDtoGroupedByStatus;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderMapperTest {

    @Test
    public void toOrderStatusDtoTest() {
        SushiOrder sushiOrder = SushiOrder.builder()
            .id(1)
            .sushi(Sushi.builder().id(1).name("Kamikaze Roll").build())
            .status(OrderStatus.CREATED.getStatus())
            .timeSpent(40)
            .createdAt(OffsetDateTime.now())
            .build();
        OrderStatusDto orderStatusDto = toOrderStatusDto(sushiOrder);
        assertAll(
            () -> assertEquals(sushiOrder.getId(), orderStatusDto.getOrderId()),
            () -> assertEquals(sushiOrder.getTimeSpent(), orderStatusDto.getTimeSpent())
        );
    }

    @Test
    public void toOrderCreationDtoTest() {
        SushiOrder sushiOrder = SushiOrder.builder()
            .id(1)
            .sushi(Sushi.builder().id(2).name("Kamikaze Roll").build())
            .status(OrderStatus.CREATED.getStatus())
            .timeSpent(40)
            .createdAt(OffsetDateTime.now())
            .build();
        OrderCreationDto orderStatusDto = toOrderCreationDto(sushiOrder);
        assertAll(
            () -> assertEquals(sushiOrder.getId(), orderStatusDto.getId()),
            () -> assertEquals(sushiOrder.getStatus().getId(), orderStatusDto.getStatusId()),
            () -> assertEquals(sushiOrder.getSushi().getId(), orderStatusDto.getSushiId()),
            () -> assertEquals(sushiOrder.getCreatedAt().toInstant().toEpochMilli(), orderStatusDto.getCreatedAt())
        );
    }

    @Test
    public void toOrderStatusDtoGroupedByStatusTest() {
        SushiOrder sushiOrderCreated = SushiOrder.builder()
            .id(1)
            .sushi(Sushi.builder().id(1).name("Kamikaze Roll").build())
            .status(OrderStatus.CREATED.getStatus())
            .timeSpent(40)
            .createdAt(OffsetDateTime.now())
            .build();
        SushiOrder sushiOrderInProgress = SushiOrder.builder()
            .id(2)
            .sushi(Sushi.builder().id(1).name("Dragon Eye").build())
            .status(OrderStatus.IN_PROGRESS.getStatus())
            .timeSpent(50)
            .createdAt(OffsetDateTime.now())
            .build();
        List<SushiOrder> sushiOrders = Arrays.asList(sushiOrderCreated, sushiOrderInProgress);
        Map<String, List<OrderStatusDto>> result = toOrderStatusDtoGroupedByStatus(sushiOrders);
        assertAll(
            () -> assertThat(result, IsMapContaining.hasEntry("created",
                Collections.singletonList(OrderStatusDto.builder().orderId(1).timeSpent(40).build()))),
            () -> assertThat(result, IsMapContaining.hasEntry("in-progress",
                Collections.singletonList(OrderStatusDto.builder().orderId(2).timeSpent(50).build())))
        );
    }

}