package com.example.weather.provider;

import com.example.weather.client.WeatherApiClient;
import com.example.weather.dto.WeatherResponse;
import com.example.weather.model.WeatherApiResponse;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

import org.springframework.stereotype.Component;

@Component
public class WeatherApiProvider
        implements WeatherProvider {

    private final WeatherApiClient client;

    public WeatherApiProvider(
            WeatherApiClient client) {

        this.client = client;
    }

    @Override
    @Retry(name = "weatherApi")
    @CircuitBreaker(
            name = "weatherApi"
    )
    public WeatherResponse getWeather(
            String city) {

        WeatherApiResponse response =
                client.getWeather(city);

        if (response == null ||
                response.location() == null ||
                response.current() == null) {

            throw new RuntimeException(
                    "Invalid WeatherAPI response"
            );
        }

        WeatherApiResponse.Location location =
                response.location();

        WeatherApiResponse.Current current =
                response.current();

        return new WeatherResponse(

                location.name(),

                location.country(),

                location.lat(),

                location.lon(),

                current.temp_c(),

                current.feelslike_c(),

                current.humidity(),

                current.wind_kph(),

                current.condition().text()
        );
    }
}