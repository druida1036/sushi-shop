package com.livebarn.sushishop.services;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.livebarn.sushishop.entities.OrderStatus;
import com.livebarn.sushishop.entities.Sushi;
import com.livebarn.sushishop.entities.SushiOrder;
import com.livebarn.sushishop.exceptions.InvalidOrderStatusException;
import com.livebarn.sushishop.exceptions.NotFoundException;
import com.livebarn.sushishop.models.OrderStatusDto;
import com.livebarn.sushishop.repositories.SushiOrderRepository;
import com.livebarn.sushishop.repositories.SushiRepository;
import org.hamcrest.collection.IsMapContaining;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SushiOrderServiceTest {

    @Mock
    private SushiRepository sushiRepository;
    @Mock
    private SushiOrderRepository sushiOrderRepository;
    @Spy
    private OrderCacheService orderCacheService;
    @Spy
    private OrderResumeCacheService orderResumeCacheService;
    @InjectMocks
    private SushiOrderService sushiOrderService;

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(sushiRepository);
        verifyNoMoreInteractions(sushiOrderRepository);
        verifyNoMoreInteractions(orderCacheService);
        verifyNoMoreInteractions(orderResumeCacheService);
    }

    @Test
    void findOrderByStatusTest() {
        List<SushiOrder> sushiOrders = Arrays.asList(
            SushiOrder.builder()
                .id(1)
                .sushi(Sushi.builder().id(1).timeToMake(30).build())
                .status(OrderStatus.CREATED.getStatus())
                .timeSpent(0)
                .build(),
            SushiOrder.builder()
                .id(2)
                .sushi(Sushi.builder().id(1).timeToMake(30).build())
                .status(OrderStatus.FINISHED.getStatus())
                .timeSpent(30)
                .build()
        );
        when(sushiOrderRepository.findAll()).thenReturn(sushiOrders);
        Map<String, List<OrderStatusDto>> orders = sushiOrderService.findOrderByStatus();

        assertAll(
            () -> assertThat(orders, IsMapContaining.hasEntry("created",
                Collections.singletonList(OrderStatusDto.builder().orderId(1).timeSpent(0).build()))),
            () -> assertThat(orders, IsMapContaining.hasEntry("finished",
                Collections.singletonList(OrderStatusDto.builder().orderId(2).timeSpent(30).build())))
        );
    }

    @Test
    void findNextOrderTest() {
        sushiOrderService.findNextOrder();

        verify(sushiOrderRepository).findFirstByStatusIdOrderByCreatedAt(OrderStatus.CREATED.getStatus().getId());
    }

    @Test
    void updateOrderTest() {
        SushiOrder sushiOrder = SushiOrder.builder()
            .id(1)
            .sushi(Sushi.builder().id(1).timeToMake(30).build())
            .status(OrderStatus.CREATED.getStatus())
            .timeSpent(0)
            .build();
        ArgumentCaptor<SushiOrder> sushiOrderArgumentCaptor = ArgumentCaptor.forClass(SushiOrder.class);

        sushiOrderService.updateOrder(sushiOrder, OrderStatus.FINISHED, 30);
        verify(sushiOrderRepository).save(sushiOrderArgumentCaptor.capture());
        verify(orderCacheService).removeOrder(1);

        assertAll(
            () -> assertEquals(OrderStatus.FINISHED.getStatus(), sushiOrderArgumentCaptor.getValue().getStatus()),
            () -> assertEquals(30, sushiOrderArgumentCaptor.getValue().getTimeSpent())
        );
    }

    @Test
    void submitOrderWithValidSushiNameTest() {
        String sushiName = "Dragon Eye";
        Sushi sushi = Sushi.builder().id(2).name(sushiName).build();
        when(sushiRepository.findByName(sushiName)).thenReturn(Optional.of(sushi));

        sushiOrderService.submitOrder(sushiName);

        ArgumentCaptor<SushiOrder> sushiOrderArgumentCaptor = ArgumentCaptor.forClass(SushiOrder.class);

        verify(sushiOrderRepository).save(sushiOrderArgumentCaptor.capture());

        assertAll(
            () -> assertEquals(0, sushiOrderArgumentCaptor.getValue().getId()),
            () -> assertEquals(sushi, sushiOrderArgumentCaptor.getValue().getSushi()),
            () -> assertEquals(OrderStatus.CREATED.getStatus(), sushiOrderArgumentCaptor.getValue().getStatus()),
            () -> assertNotNull(sushiOrderArgumentCaptor.getValue().getCreatedAt()),
            () -> assertEquals(0, sushiOrderArgumentCaptor.getValue().getTimeSpent())
        );
    }

    @Test
    void submitOrderWithInvalidSushiNameTest() {
        String sushiName = "Dragon Eye1";
        when(sushiRepository.findByName(sushiName)).thenReturn(Optional.empty());

        Exception exception = assertThrows(NotFoundException.class,
            () -> sushiOrderService.submitOrder(sushiName));

        assertEquals("Sushi with name: [Dragon Eye1] not found.", exception.getMessage());
    }

    @Test
    void cancelOrderWithValidStatusTest() {
        SushiOrder sushiOrder = SushiOrder.builder()
            .id(1)
            .sushi(Sushi.builder().id(1).timeToMake(30).build())
            .status(OrderStatus.CREATED.getStatus())
            .timeSpent(0)
            .createdAt(OffsetDateTime.now())
            .build();
        when(sushiOrderRepository.findById(1)).thenReturn(Optional.of(sushiOrder));

        sushiOrderService.cancelOrder(1);

        ArgumentCaptor<SushiOrder> sushiOrderArgumentCaptor = ArgumentCaptor.forClass(SushiOrder.class);

        verify(sushiOrderRepository).save(sushiOrderArgumentCaptor.capture());
        verify(orderCacheService).addOrder(1, OrderStatus.CANCELLED);

        assertAll(
            () -> assertEquals(sushiOrder.getId(), sushiOrderArgumentCaptor.getValue().getId()),
            () -> assertEquals(OrderStatus.CANCELLED.getStatus(), sushiOrderArgumentCaptor.getValue().getStatus()),
            () -> assertEquals(sushiOrder.getSushi(), sushiOrderArgumentCaptor.getValue().getSushi()),
            () -> assertEquals(sushiOrder.getCreatedAt(), sushiOrderArgumentCaptor.getValue().getCreatedAt()),
            () -> assertEquals(sushiOrder.getTimeSpent(), sushiOrderArgumentCaptor.getValue().getTimeSpent())
        );
    }

    @Test
    void cancelOrderWithInvalidStatusTest() {
        SushiOrder sushiOrder = SushiOrder.builder()
            .id(1)
            .sushi(Sushi.builder().id(1).timeToMake(30).build())
            .status(OrderStatus.CANCELLED.getStatus())
            .timeSpent(10)
            .createdAt(OffsetDateTime.now())
            .build();
        when(sushiOrderRepository.findById(1)).thenReturn(Optional.of(sushiOrder));

        Exception exception = assertThrows(InvalidOrderStatusException.class,
            () -> sushiOrderService.cancelOrder(1));

        assertEquals("Order in [cancelled] can not be cancelled.", exception.getMessage());
    }

    @Test
    void cancelOrderNotExistingTest() {
        when(sushiOrderRepository.findById(1)).thenReturn(Optional.empty());

        Exception exception = assertThrows(NotFoundException.class,
            () -> sushiOrderService.cancelOrder(1));

        assertEquals("Order with id [1] not found.", exception.getMessage());
    }

    @Test
    void pauseOrderWithValidStatusTest() {
        SushiOrder sushiOrder = SushiOrder.builder()
            .id(1)
            .sushi(Sushi.builder().id(1).timeToMake(30).build())
            .status(OrderStatus.CREATED.getStatus())
            .timeSpent(0)
            .createdAt(OffsetDateTime.now())
            .build();
        when(sushiOrderRepository.findById(1)).thenReturn(Optional.of(sushiOrder));

        sushiOrderService.pauseOrder(1);

        ArgumentCaptor<SushiOrder> sushiOrderArgumentCaptor = ArgumentCaptor.forClass(SushiOrder.class);

        verify(sushiOrderRepository).save(sushiOrderArgumentCaptor.capture());
        verify(orderCacheService).addOrder(1, OrderStatus.PAUSED);

        assertAll(
            () -> assertEquals(sushiOrder.getId(), sushiOrderArgumentCaptor.getValue().getId()),
            () -> assertEquals(OrderStatus.PAUSED.getStatus(), sushiOrderArgumentCaptor.getValue().getStatus()),
            () -> assertEquals(sushiOrder.getSushi(), sushiOrderArgumentCaptor.getValue().getSushi()),
            () -> assertEquals(sushiOrder.getCreatedAt(), sushiOrderArgumentCaptor.getValue().getCreatedAt()),
            () -> assertEquals(sushiOrder.getTimeSpent(), sushiOrderArgumentCaptor.getValue().getTimeSpent())
        );
    }

    @Test
    void resumeOrderWithValidStatusTest() {
        SushiOrder sushiOrder = SushiOrder.builder()
            .id(1)
            .sushi(Sushi.builder().id(1).timeToMake(30).build())
            .status(OrderStatus.PAUSED.getStatus())
            .timeSpent(10)
            .createdAt(OffsetDateTime.now())
            .build();
        when(sushiOrderRepository.findById(1)).thenReturn(Optional.of(sushiOrder));

        sushiOrderService.resumeOrder(1);

        ArgumentCaptor<SushiOrder> sushiOrderArgumentCaptor = ArgumentCaptor.forClass(SushiOrder.class);

        verify(sushiOrderRepository).save(sushiOrderArgumentCaptor.capture());
        verify(orderResumeCacheService).add(sushiOrder);

        assertAll(
            () -> assertEquals(sushiOrder.getId(), sushiOrderArgumentCaptor.getValue().getId()),
            () -> assertEquals(OrderStatus.IN_PROGRESS.getStatus(), sushiOrderArgumentCaptor.getValue().getStatus()),
            () -> assertEquals(sushiOrder.getSushi(), sushiOrderArgumentCaptor.getValue().getSushi()),
            () -> assertEquals(sushiOrder.getCreatedAt(), sushiOrderArgumentCaptor.getValue().getCreatedAt()),
            () -> assertEquals(sushiOrder.getTimeSpent(), sushiOrderArgumentCaptor.getValue().getTimeSpent())
        );
    }

    @Test
    void resumeOrderWithInvalidStatusTest() {
        SushiOrder sushiOrder = SushiOrder.builder()
            .id(1)
            .sushi(Sushi.builder().id(1).timeToMake(30).build())
            .status(OrderStatus.CREATED.getStatus())
            .timeSpent(10)
            .createdAt(OffsetDateTime.now())
            .build();
        when(sushiOrderRepository.findById(1)).thenReturn(Optional.of(sushiOrder));

        Exception exception = assertThrows(InvalidOrderStatusException.class,
            () -> sushiOrderService.resumeOrder(1));

        assertEquals("Order in [created] can not be paused.", exception.getMessage());
    }
}