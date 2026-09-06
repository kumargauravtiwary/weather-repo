package com.example.weather.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(
            LocationNotFoundException.class
    )
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleLocation(
            LocationNotFoundException ex) {

        return Map.of(
                "timestamp", Instant.now(),
                "status", 404,
                "error", "Location Not Found",
                "message", ex.getMessage()
        );
    }

    @ExceptionHandler(
            RuntimeException.class
    )
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public Map<String, Object> handleRuntime(
            RuntimeException ex) {

        return Map.of(
                "timestamp", Instant.now(),
                "status", 502,
                "error", "Weather Provider Unavailable",
                "message", ex.getMessage()
        );
    }
}