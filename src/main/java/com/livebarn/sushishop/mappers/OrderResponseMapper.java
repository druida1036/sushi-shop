package com.livebarn.sushishop.mappers;

import java.util.List;
import java.util.Map;

import com.livebarn.sushishop.models.OrderCreationDto;
import com.livebarn.sushishop.models.OrderCreationResponse;
import com.livebarn.sushishop.models.OrderStatusDto;
import com.livebarn.sushishop.models.OrderStatusResponse;
import lombok.experimental.UtilityClass;

@UtilityClass
public class OrderResponseMapper {

    public static OrderStatusResponse toOrderStatusResponse(Map<String, List<OrderStatusDto>> orders) {
        return OrderStatusResponse.builder().orders(orders).build();
    }

    public static OrderCreationResponse toOrderCreationResponse(OrderCreationDto order) {
        return OrderCreationResponse.builder().order(order).build();
    }

}
