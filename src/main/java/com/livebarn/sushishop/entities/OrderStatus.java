package com.livebarn.sushishop.entities;

import lombok.Getter;

@Getter
public enum OrderStatus {
    CREATED(1, "created"),
    IN_PROGRESS(2, "in-progress"),
    PAUSED(3, "paused"),
    FINISHED(4, "finished"),
    CANCELLED(5, "cancelled");


    private final Status status;

    OrderStatus(int id, String name) {
        status = Status.builder().id(id).name(name).build();
    }

    public Status getStatus() {
        return status;
    }
}
