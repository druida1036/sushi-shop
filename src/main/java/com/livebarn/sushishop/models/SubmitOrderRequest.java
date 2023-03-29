package com.livebarn.sushishop.models;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Getter
public class SubmitOrderRequest {
    @JsonAlias("sushi_name")
    private String sushiName;

}
