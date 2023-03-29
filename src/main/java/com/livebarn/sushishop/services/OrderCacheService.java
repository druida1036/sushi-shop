package com.livebarn.sushishop.services;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.livebarn.sushishop.entities.OrderStatus;
import org.springframework.stereotype.Service;

/**
 * This an in memory cache implementation that holds the orders changed to 'paused' or 'cancelled' This cache helps the
 * chef to react to any order change without query to a DB improving the performance.
 */
@Service
public class OrderCacheService {
    private final Map<Integer, OrderStatus> orders = new ConcurrentHashMap<>();

    public void addOrder(final int orderId, OrderStatus status) {
        orders.put(orderId, status);
    }

    public Optional<OrderStatus> getOrderStatus(final int orderId) {
        return Optional.ofNullable(orders.get(orderId));
    }

    public void removeOrder(final int orderId) {
        orders.remove(orderId);
    }


}
