package com.livebarn.sushishop.models;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Response<T> {
    private int code;
    private String msg;
    private T body;

    public Response(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    @JsonUnwrapped
    public T getBody() {
        return body;
    }
}
