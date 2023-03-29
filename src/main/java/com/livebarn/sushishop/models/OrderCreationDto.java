package com.livebarn.sushishop.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class OrderCreationDto {
    private int id;
    private int statusId;
    private int sushiId;
    private long createdAt;
}
