package com.livebarn.sushishop.services;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.transaction.Transactional;

import com.livebarn.sushishop.entities.OrderStatus;
import com.livebarn.sushishop.entities.Sushi;
import com.livebarn.sushishop.entities.SushiOrder;
import com.livebarn.sushishop.exceptions.InvalidOrderStatusException;
import com.livebarn.sushishop.exceptions.NotFoundException;
import com.livebarn.sushishop.mappers.OrderMapper;
import com.livebarn.sushishop.models.OrderCreationDto;
import com.livebarn.sushishop.models.OrderStatusDto;
import com.livebarn.sushishop.repositories.SushiOrderRepository;
import com.livebarn.sushishop.repositories.SushiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SushiOrderService {
    private static final String ORDER_NOT_FOUND = "Order with id [%s] not found.";
    private static final String SUSHI_NOT_FOUND = "Sushi with name: [%s] not found.";
    private static final String ORDER_INVALID_STATUS = "Order in [%s] can not be %s.";

    private static final Set<Integer> INVALID_CANCELLED_ORDER_STATUSES =
        Collections.unmodifiableSet(Stream.of(OrderStatus.FINISHED.getStatus().getId(),
            OrderStatus.CANCELLED.getStatus().getId()).collect(Collectors.toSet()));

    private static final Set<Integer> INVALID_PAUSED_ORDER_STATUSES =
        Collections.unmodifiableSet(Stream.of(OrderStatus.FINISHED.getStatus().getId(),
                OrderStatus.CANCELLED.getStatus().getId(), OrderStatus.PAUSED.getStatus().getId())
            .collect(Collectors.toSet()));
    private final SushiOrderRepository sushiOrderRepository;
    private final SushiRepository sushiRepository;
    private final OrderCacheService orderCacheService;
    private final OrderResumeCacheService orderResumeCacheService;

    private static void validateOrderStatus(final Set<Integer> invalidOrderStatuses, final String action,
        final SushiOrder sushiOrder) {
        if (invalidOrderStatuses.contains(sushiOrder.getStatus().getId())) {
            throwInvalidOrderStatus(sushiOrder.getStatus().getName(), action);
        }
    }

    private static void throwInvalidOrderStatus(final String statusName, final String action) {
        throw new InvalidOrderStatusException(String.format(ORDER_INVALID_STATUS, statusName, action));
    }

    public Map<String, List<OrderStatusDto>> findOrderByStatus() {
        return OrderMapper.toOrderStatusDtoGroupedByStatus(sushiOrderRepository.findAll());
    }

    public Optional<SushiOrder> findNextOrder() {
        return sushiOrderRepository.findFirstByStatusIdOrderByCreatedAt(OrderStatus.CREATED.getStatus().getId());
    }

    public void updateOrder(final SushiOrder sushiOrder, final OrderStatus status, final int timeSpent) {
        sushiOrder.setTimeSpent(timeSpent);
        sushiOrder.setStatus(status.getStatus());
        orderCacheService.removeOrder(sushiOrder.getId());
        sushiOrderRepository.save(sushiOrder);
    }

    @Transactional
    public OrderCreationDto submitOrder(final String sushiName) {
        final Sushi sushi = findSushiOrThrow(sushiName);
        final SushiOrder sushiOrder = buildSushiOrder(sushi);
        sushiOrderRepository.save(sushiOrder);
        return OrderMapper.toOrderCreationDto(sushiOrder);
    }

    @Transactional
    public void cancelOrder(final int orderId) {
        final SushiOrder sushiOrder = findOrderByIdOrThrow(orderId);
        validateOrderStatus(SushiOrderService.INVALID_CANCELLED_ORDER_STATUSES,
            OrderStatus.CANCELLED.getStatus().getName(), sushiOrder
        );
        sushiOrder.setStatus(OrderStatus.CANCELLED.getStatus());
        sushiOrderRepository.save(sushiOrder);
        orderCacheService.addOrder(orderId, OrderStatus.CANCELLED);
    }

    @Transactional
    public void pauseOrder(final int orderId) {
        final SushiOrder sushiOrder = findOrderByIdOrThrow(orderId);
        validateOrderStatus(SushiOrderService.INVALID_PAUSED_ORDER_STATUSES, OrderStatus.PAUSED.getStatus().getName(),
            sushiOrder
        );
        sushiOrder.setStatus(OrderStatus.PAUSED.getStatus());
        sushiOrderRepository.save(sushiOrder);
        orderCacheService.addOrder(orderId, OrderStatus.PAUSED);

    }

    @Transactional
    public void resumeOrder(final int orderId) {
        final SushiOrder sushiOrder = findOrderByIdOrThrow(orderId);
        if (OrderStatus.PAUSED.getStatus().getId() != sushiOrder.getStatus().getId()) {
            throwInvalidOrderStatus(sushiOrder.getStatus().getName(), OrderStatus.PAUSED.getStatus().getName());
        }
        sushiOrder.setStatus(OrderStatus.IN_PROGRESS.getStatus());
        sushiOrderRepository.save(sushiOrder);
        orderResumeCacheService.add(sushiOrder);

    }

    private SushiOrder findOrderByIdOrThrow(final int orderId) {
        return sushiOrderRepository.findById(orderId)
            .orElseThrow(() -> {
                throw new NotFoundException(String.format(ORDER_NOT_FOUND, orderId));
            });
    }

    private SushiOrder buildSushiOrder(final Sushi sushi) {
        return SushiOrder.builder()
            .sushi(sushi)
            .status(OrderStatus.CREATED.getStatus())
            .createdAt(OffsetDateTime.now())
            .build();
    }

    private Sushi findSushiOrThrow(final String sushiName) {
        return sushiRepository.findByName(sushiName).orElseThrow(() -> {
            throw new NotFoundException(String.format(SUSHI_NOT_FOUND, sushiName));
        });
    }

}
