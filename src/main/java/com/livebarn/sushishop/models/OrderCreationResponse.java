package com.livebarn.sushishop.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderCreationResponse {

    private final OrderCreationDto order;

}
