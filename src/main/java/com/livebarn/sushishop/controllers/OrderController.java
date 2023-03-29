package com.livebarn.sushishop.controllers;

import com.livebarn.sushishop.mappers.ResponseMapper;
import com.livebarn.sushishop.models.OrderCreationResponse;
import com.livebarn.sushishop.models.OrderStatusResponse;
import com.livebarn.sushishop.models.Response;
import com.livebarn.sushishop.models.SubmitOrderRequest;
import com.livebarn.sushishop.services.SushiOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final SushiOrderService sushiOrderService;

    @Operation(summary = "Find the order by status")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Found the book",
        content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Response.class))}),})
    @GetMapping
    public Response<OrderStatusResponse> getOrdersByStatus() {
        return ResponseMapper.toSuccessRecordsFoundResponse(sushiOrderService.findOrderByStatus());
    }

    @Operation(summary = "Submit Order by sushi name")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Order submitted successfully",
        content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Response.class))}),
        @ApiResponse(responseCode = "404", description = "Invalid sushi name supplied",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Response.class)))})
    @PostMapping
    public ResponseEntity<Response<OrderCreationResponse>> submit(
        @RequestBody final SubmitOrderRequest submitOrderRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseMapper.toSuccessSubmittedOrderResponse(
            sushiOrderService.submitOrder(submitOrderRequest.getSushiName())));
    }

    @Operation(summary = "Cancel Order by Id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order cancelled successfully",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Response.class))}),
        @ApiResponse(responseCode = "404", description = "Order not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Response.class)))})
    @DeleteMapping("/{order_id}")
    public Response<?> cancel(@PathVariable("order_id") final int orderId) {
        sushiOrderService.cancelOrder(orderId);
        return ResponseMapper.toSuccessCancelledOrderResponse();
    }

    @Operation(summary = "Pause Order by Id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order paused successfully",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Response.class))}),
        @ApiResponse(responseCode = "404", description = "Order not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Response.class)))})
    @PutMapping("/{order_id}/pause")
    public Response<?> pause(@PathVariable("order_id") final int orderId) {
        sushiOrderService.pauseOrder(orderId);
        return ResponseMapper.toSuccessPausedOrderResponse();
    }

    @Operation(summary = "Resume Order by Id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order resumed successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Response.class))),
        @ApiResponse(responseCode = "404", description = "Order not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Response.class)))})
    @PutMapping("/{order_id}/resume")
    public Response<?> resume(@PathVariable("order_id") final int orderId) {
        sushiOrderService.resumeOrder(orderId);
        return ResponseMapper.toSuccessResumedOrderResponse();
    }
}
