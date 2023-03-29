package com.livebarn.sushishop.models;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderStatusResponse {

    private final Map<String, List<OrderStatusDto>> orders;

    @JsonAnyGetter
    public Map<String, List<OrderStatusDto>> getOrders() {
        return orders;
    }
}
