package com.livebarn.sushishop.services;

import java.util.Optional;

import com.livebarn.sushishop.entities.OrderStatus;
import com.livebarn.sushishop.entities.Sushi;
import com.livebarn.sushishop.entities.SushiOrder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class OrderProcessorTest {

    @Mock
    private SushiOrderService sushiOrderService;
    @Spy
    private OrderCacheService orderCacheService;
    @Spy
    private OrderResumeCacheService orderResumeCacheService;
    @InjectMocks
    private OrderProcessor orderProcessor;

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(sushiOrderService);
        verifyNoMoreInteractions(orderCacheService);
        verifyNoMoreInteractions(orderResumeCacheService);
    }

    @Test
    void processEmptyOrderShouldNotUpdate() {
        doReturn(Optional.empty()).when(sushiOrderService).findNextOrder();
        orderProcessor.process();

        verify(orderResumeCacheService, times(1)).retrieve();
        verify(sushiOrderService, times(1)).findNextOrder();
    }

    @Test
    void processOrderShouldBeCompleted() {
        SushiOrder sushiOrder = SushiOrder.builder().id(1)
            .sushi(Sushi.builder().timeToMake(30).build())
            .build();
        doReturn(Optional.of(sushiOrder)).when(sushiOrderService).findNextOrder();
        orderProcessor.process();

        verify(orderResumeCacheService).retrieve();
        verify(sushiOrderService).findNextOrder();
        verify(orderCacheService, times(30)).getOrderStatus(1);

        verify(sushiOrderService).updateOrder(sushiOrder, OrderStatus.IN_PROGRESS, 0);
        verify(sushiOrderService).updateOrder(sushiOrder, OrderStatus.FINISHED, 30);
    }

    @Test
    void processEmptyOrderShouldBeResumed() {
        SushiOrder sushiOrder = SushiOrder.builder().id(1)
            .sushi(Sushi.builder().timeToMake(30).build())
            .timeSpent(10)
            .build();
        doReturn(Optional.of(sushiOrder)).when(orderResumeCacheService).retrieve();
        orderProcessor.process();

        verify(orderResumeCacheService).retrieve();
        // process only the remaining time
        verify(orderCacheService, times(20)).getOrderStatus(1);

        verify(sushiOrderService).updateOrder(sushiOrder, OrderStatus.IN_PROGRESS, 10);
        verify(sushiOrderService).updateOrder(sushiOrder, OrderStatus.FINISHED, 30);
    }


    @Test
    void processOrderShouldBePaused() {
        SushiOrder sushiOrder = SushiOrder.builder().id(1)
            .sushi(Sushi.builder().timeToMake(30).build())
            .status(OrderStatus.IN_PROGRESS.getStatus())
            .build();

        doReturn(Optional.of(sushiOrder)).when(sushiOrderService).findNextOrder();
        doReturn(Optional.of(OrderStatus.PAUSED)).when(orderCacheService).getOrderStatus(1);
        orderProcessor.process();

        verify(orderResumeCacheService).retrieve();
        verify(orderCacheService).getOrderStatus(1);

        verify(sushiOrderService).updateOrder(sushiOrder, OrderStatus.IN_PROGRESS, 0);
        verify(sushiOrderService).updateOrder(sushiOrder, OrderStatus.PAUSED, 1);
    }

    @Test
    void processOrderShouldBeCancelled() {
        SushiOrder sushiOrder = SushiOrder.builder().id(1)
            .sushi(Sushi.builder().timeToMake(30).build())
            .status(OrderStatus.IN_PROGRESS.getStatus())
            .build();

        doReturn(Optional.of(sushiOrder)).when(sushiOrderService).findNextOrder();
        doReturn(Optional.of(OrderStatus.CANCELLED)).when(orderCacheService).getOrderStatus(1);
        orderProcessor.process();

        verify(orderResumeCacheService).retrieve();
        verify(orderCacheService).getOrderStatus(1);

        verify(sushiOrderService).updateOrder(sushiOrder, OrderStatus.IN_PROGRESS, 0);
        verify(sushiOrderService).updateOrder(sushiOrder, OrderStatus.CANCELLED, 1);
    }
}