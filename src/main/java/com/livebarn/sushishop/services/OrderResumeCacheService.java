package com.livebarn.sushishop.services;

import java.util.Optional;
import java.util.Stack;

import com.livebarn.sushishop.entities.SushiOrder;
import org.springframework.stereotype.Service;

/**
 * This an in memory stack implementation that holds the resumed orders and It is used by the OrderProcessor to
 * prioritize that orders
 */
@Service
public class OrderResumeCacheService {
    private final Stack<SushiOrder> orders = new Stack<>();

    public void add(final SushiOrder sushiOrder) {
        orders.push(sushiOrder);
    }

    public Optional<SushiOrder> retrieve() {
        return orders.isEmpty() ? Optional.empty() : Optional.of(orders.pop());
    }


}
