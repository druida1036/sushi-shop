package com.livebarn.sushishop.controllers;

import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.livebarn.sushishop.exceptions.InvalidOrderStatusException;
import com.livebarn.sushishop.exceptions.NotFoundException;
import com.livebarn.sushishop.mappers.ResponseMapper;
import com.livebarn.sushishop.models.OrderCreationDto;
import com.livebarn.sushishop.models.OrderStatusDto;
import com.livebarn.sushishop.models.SubmitOrderRequest;
import com.livebarn.sushishop.services.SushiOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
public class OrderControllerTest {

    private MockMvc mockMvc;
    @MockBean
    private SushiOrderService sushiOrderService;
    //    @MockBean
    private ResponseMapper responseMapper;
    @Autowired
    private OrderController orderController;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
            .setControllerAdvice(new ControllerExceptionHandler())
            .build();
    }

    @Test
    public void ordersShouldReturnOrdersGroupedByStatus() throws Exception {
        Map<String, List<OrderStatusDto>> orders = Collections.singletonMap("created",
            Collections.singletonList(OrderStatusDto.builder().orderId(1).timeSpent(0).build()));

        given(sushiOrderService.findOrderByStatus())
            .willReturn(orders);

        ResultActions response = mockMvc.perform(get("/orders"));

        response.andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("Orders found"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.created[0].orderId").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.created[0].timeSpent").value(0));
    }

    @Test
    public void submitOrdersShouldSubmitOrderSuccessfully() throws Exception {
        long createdAt = GregorianCalendar.getInstance().toInstant().toEpochMilli();
        OrderCreationDto orderCreationDto = OrderCreationDto.builder()
            .id(1)
            .statusId(1)
            .sushiId(1)
            .createdAt(createdAt)
            .build();

        String sushiName = "California Roll";
        given(sushiOrderService.submitOrder(sushiName)).willReturn(orderCreationDto);

        mockMvc.perform(MockMvcRequestBuilders
                .post("/orders")
                .content(toJson(new SubmitOrderRequest(sushiName)))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("Order created"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.order.id").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.order.statusId").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.order.sushiId").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.order.createdAt").value(createdAt));
    }

    @Test
    public void submitOrdersWithInvalidSushiShouldResponseNotFound() throws Exception {
        long createdAt = GregorianCalendar.getInstance().toInstant().toEpochMilli();
        OrderCreationDto orderCreationDto = OrderCreationDto.builder()
            .id(1)
            .statusId(1)
            .sushiId(1)
            .createdAt(createdAt)
            .build();

        String sushiName = "invalid name";

        String message = String.format("Sushi with name: [%s] not found.", sushiName);
        willThrow(new NotFoundException(message)).given(sushiOrderService).submitOrder(sushiName);

        mockMvc.perform(MockMvcRequestBuilders
                .post("/orders")
                .content(toJson(new SubmitOrderRequest(sushiName)))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value(message));
    }

    @Test
    public void cancelOrdersShouldResponseSuccessfully() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders
                .delete("/orders/{order_id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("Order cancelled"));
    }

    @Test
    public void cancelOrdersWithInvalidStatusShouldResponseBadRequest() throws Exception {
        willThrow(new InvalidOrderStatusException("Order in [finished] can not be cancelled.")).given(sushiOrderService)
            .cancelOrder(1);

        mockMvc.perform(MockMvcRequestBuilders
                .delete("/orders/{order_id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(2))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("Order in [finished] can not be cancelled."));
    }

    @Test
    public void cancelOrdersWithInvalidOrderIdShouldResponseNotFound() throws Exception {
        willThrow(new NotFoundException("Order with id [1] not found.")).given(sushiOrderService)
            .cancelOrder(1);

        mockMvc.perform(MockMvcRequestBuilders
                .delete("/orders/{order_id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("Order with id [1] not found."));
    }

    @Test
    public void cancelOrdersWithSystemErrorShouldResponseInternalError() throws Exception {
        willThrow(new RuntimeException("exception stack trace")).given(sushiOrderService)
            .cancelOrder(1);

        mockMvc.perform(MockMvcRequestBuilders
                .delete("/orders/{order_id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(3))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.msg").value(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()));
    }

    @Test
    public void pauseOrdersShouldResponseSuccessfully() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                .put("/orders/{order_id}/pause", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("Order paused"));
    }

    @Test
    public void pauseOrdersWithInvalidStatusShouldResponseBadRequest() throws Exception {
        willThrow(new InvalidOrderStatusException("Order in [finished] can not be paused.")).given(sushiOrderService)
            .pauseOrder(1);

        mockMvc.perform(MockMvcRequestBuilders
                .put("/orders/{order_id}/pause", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(2))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("Order in [finished] can not be paused."));
    }

    @Test
    public void resumeOrdersShouldResponseSuccessfully() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                .put("/orders/{order_id}/resume", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("Order resumed"));
    }

    @Test
    public void resumeOrdersWithInvalidStatusShouldResponseBadRequest() throws Exception {
        willThrow(new InvalidOrderStatusException("Order in [finished] can not be resumed.")).given(sushiOrderService)
            .resumeOrder(1);

        mockMvc.perform(MockMvcRequestBuilders
                .put("/orders/{order_id}/resume", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(2))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("Order in [finished] can not be resumed."));
    }

    private String toJson(final Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

}
