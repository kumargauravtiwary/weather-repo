package com.example.weather.exception;

public class ExternalWeatherApiException
        extends RuntimeException {

    public ExternalWeatherApiException(

            String message,

            Throwable cause

    ) {

        super(message, cause);
    }
}