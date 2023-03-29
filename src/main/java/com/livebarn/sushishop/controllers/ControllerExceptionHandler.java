package com.livebarn.sushishop.controllers;

import com.livebarn.sushishop.exceptions.InvalidOrderStatusException;
import com.livebarn.sushishop.exceptions.NotFoundException;
import com.livebarn.sushishop.mappers.ResponseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@RequiredArgsConstructor
public class ControllerExceptionHandler {

    @ExceptionHandler(value = NotFoundException.class)
    protected ResponseEntity<Object> handleNotFound(final RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseMapper.toErrorResponse(1, ex.getMessage()));
    }

    @ExceptionHandler(value = InvalidOrderStatusException.class)
    protected ResponseEntity<Object> handleInvalidOrderStatus(final RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseMapper.toErrorResponse(2, ex.getMessage()));
    }

    @ExceptionHandler(value = Exception.class)
    protected ResponseEntity<Object> handleException(final RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseMapper.toErrorResponse(3,
            HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()));
    }
}

/**
 * Problem Definition Decitions of design Archited un
 */