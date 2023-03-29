package com.livebarn.sushishop.services;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.livebarn.sushishop.entities.OrderStatus;
import com.livebarn.sushishop.entities.SushiOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderProcessor {
    private final SushiOrderService sushiOrderService;
    private final OrderCacheService orderCacheService;
    private final OrderResumeCacheService orderResumeCacheService;
    @Value("${sushi.shop.chef.order.review-time-rate:1}")
    private int chefReviewRate;

    @Value("${sushi.shop.chef.active-review:true}")
    private boolean chefActiveReview;

    @Async("chefManagerExecutor")
    // This represents chef manager that is monitoring periodically if there is pending orders
    @Scheduled(fixedRateString = "${sushi.shop.chef-manager.order.review-time-rate:1000}")
    public void process() {
        Optional<SushiOrder> sushiOrder = findNextOrder();
        // The chef is eagerly searching for new orders to process
        while (sushiOrder.isPresent()) {
            manageOrder(sushiOrder.get());
            sushiOrder = chefActiveReview ? findNextOrder() : Optional.empty();
        }
    }

    private Optional<SushiOrder> findNextOrder() {
        // Give priority to resumed orders
        final Optional<SushiOrder> resumeOrder = orderResumeCacheService.retrieve();
        return resumeOrder.isPresent() ? resumeOrder : sushiOrderService.findNextOrder();
    }

    /**
     * This method is responsible to process the steps to process a given order. A given order require 3 steps 1. Update
     * the status order to 'in-progress' 2. Prepare the order. 3. Update the order with the final order status and the
     * time spent by the chef to prepare it. The final order statuses are 'finished', 'paused' or 'cancelled'.
     */
    private void manageOrder(final SushiOrder sushiOrder) {
        try {
            log.info("The order {} is {}", sushiOrder.getId(), OrderStatus.IN_PROGRESS.getStatus().getName());
            sushiOrderService.updateOrder(sushiOrder, OrderStatus.IN_PROGRESS, sushiOrder.getTimeSpent());
            PreparationResult preparationResult = prepareFood(sushiOrder);
            sushiOrderService.updateOrder(sushiOrder, preparationResult.getStatus(), preparationResult.getTimeSpent());
            log.info("The order {} is {}", sushiOrder.getId(), preparationResult.getStatus().getStatus().getName());
        } catch (InterruptedException e) {
            throw new IllegalStateException("task interrupted", e);
        }
    }

    /**
     * This method simulate the preparation of a given order. It is responsible to compute the time spent in each order
     * and verify if the order in progress has changed to cancelled or paused periodically. If the order status is
     * changed to 'cancelled' or 'paused' then the progress is stopped and saved in the DB Otherwise the order is
     * completed and saved to the DB
     *
     * @param sushiOrder is order to process
     * @return The PreparationResult contains the order status and the time spent in a given order
     */
    private PreparationResult prepareFood(final SushiOrder sushiOrder) throws InterruptedException {
        Optional<OrderStatus> orderStatus = Optional.empty();
        int timeSpent = sushiOrder.getTimeSpent();
        while (!orderStatus.isPresent() && timeSpent < sushiOrder.getSushi().getTimeToMake()) {
            TimeUnit.SECONDS.sleep(chefReviewRate);
            //Check if the order has changed
            orderStatus = reviewOrderStatus(sushiOrder);
            timeSpent++;
        }
        return new PreparationResult(timeSpent, orderStatus.orElse(OrderStatus.FINISHED));
    }

    private Optional<OrderStatus> reviewOrderStatus(final SushiOrder sushiOrder) {
        return orderCacheService.getOrderStatus(sushiOrder.getId());
    }

    @Getter
    @AllArgsConstructor
    private static class PreparationResult {
        private int timeSpent;
        private OrderStatus status;
    }
}
