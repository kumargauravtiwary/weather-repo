package com.example.weather.provider;

import com.example.weather.client.OpenMeteoClient;
import com.example.weather.dto.WeatherResponse;
import com.example.weather.model.ForecastResponse;
import com.example.weather.model.GeocodingResponse;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

import org.springframework.stereotype.Component;

@Component
public class OpenMeteoProvider implements WeatherProvider {

    private final OpenMeteoClient client;

    public OpenMeteoProvider(OpenMeteoClient client) {
        this.client = client;
    }

    @Override
    @Retry(name = "openMeteo")
    @CircuitBreaker(
            name = "openMeteo",
            fallbackMethod = "fallback"
    )
    public WeatherResponse getWeather(String city) {

        GeocodingResponse.Location location =
                client.geocode(city);

        ForecastResponse forecast =
                client.getForecast(
                        location.latitude(),
                        location.longitude()
                );

        if (forecast == null ||
                forecast.current() == null) {

            throw new RuntimeException(
                    "Invalid Open-Meteo response"
            );
        }

        ForecastResponse.Current current =
                forecast.current();

        return new WeatherResponse(

                location.name(),

                location.country(),

                location.latitude(),

                location.longitude(),

                current.temperature_2m(),

                current.apparent_temperature(),

                current.relative_humidity_2m(),

                current.wind_speed_10m(),

                weatherDescription(
                        current.weather_code()
                )
        );
    }

    public WeatherResponse fallback(
            String city,
            Throwable throwable) {

        throw new RuntimeException(
                "Open-Meteo provider unavailable",
                throwable
        );
    }

    private String weatherDescription(int code) {

        return switch (code) {

            case 0 ->
                    "Clear sky";

            case 1, 2, 3 ->
                    "Mainly clear / partly cloudy / overcast";

            case 45, 48 ->
                    "Fog";

            case 51, 53, 55, 56, 57 ->
                    "Drizzle";

            case 61, 63, 65, 66, 67 ->
                    "Rain";

            case 71, 73, 75, 77 ->
                    "Snow";

            case 80, 81, 82 ->
                    "Rain showers";

            case 85, 86 ->
                    "Snow showers";

            case 95 ->
                    "Thunderstorm";

            case 96, 99 ->
                    "Thunderstorm with hail";

            default ->
                    "Unknown";
        };
    }
}