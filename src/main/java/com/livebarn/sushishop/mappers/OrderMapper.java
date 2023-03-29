package com.livebarn.sushishop.mappers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.livebarn.sushishop.entities.SushiOrder;
import com.livebarn.sushishop.models.OrderCreationDto;
import com.livebarn.sushishop.models.OrderStatusDto;
import lombok.experimental.UtilityClass;

@UtilityClass
/**
 *  This class is responsible to convert SushiOrder entity to its model representation
 */
public class OrderMapper {

    public static OrderCreationDto toOrderCreationDto(final SushiOrder sushiOrder) {
        return OrderCreationDto.builder()
            .id(sushiOrder.getId())
            .sushiId(sushiOrder.getSushi().getId())
            .statusId(sushiOrder.getStatus().getId())
            .createdAt(sushiOrder.getCreatedAt().toInstant().toEpochMilli())
            .build();
    }

    public static OrderStatusDto toOrderStatusDto(final SushiOrder sushiOrder) {
        return OrderStatusDto.builder()
            .orderId(sushiOrder.getId())
            .timeSpent(sushiOrder.getTimeSpent())
            .build();
    }

    public static Map<String, List<OrderStatusDto>> toOrderStatusDtoGroupedByStatus(
        final List<SushiOrder> sushiOrders) {
        return sushiOrders.stream().collect(Collectors.groupingBy(sushiOrder -> sushiOrder.getStatus().getName(),
            Collectors.mapping(OrderMapper::toOrderStatusDto, Collectors.toList())));

    }
}
