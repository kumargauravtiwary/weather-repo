package com.example.weather.service;

import com.example.weather.dto.WeatherResponse;
import com.example.weather.provider.OpenMeteoProvider;
import com.example.weather.provider.WeatherApiProvider;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class WeatherService {

    private final OpenMeteoProvider openMeteoProvider;

    private final WeatherApiProvider weatherApiProvider;

    public WeatherService(

            OpenMeteoProvider openMeteoProvider,

            WeatherApiProvider weatherApiProvider) {

        this.openMeteoProvider = openMeteoProvider;

        this.weatherApiProvider = weatherApiProvider;
    }

    @Cacheable(
            value = "weather",
            key = "#city.trim().toLowerCase()"
    )
    public WeatherResponse getWeather(
            String city) {

        System.out.println(
                "Redis cache MISS: " + city
        );

        try {

            System.out.println(
                    "Calling primary provider: Open-Meteo"
            );

            return openMeteoProvider
                    .getWeather(city);

        } catch (Exception primaryException) {

            System.out.println(
                    "Open-Meteo failed. "
                    + "Switching to WeatherAPI."
            );

            try {

                return weatherApiProvider
                        .getWeather(city);

            } catch (Exception secondaryException) {

                throw new RuntimeException(

                        "All weather providers are unavailable",

                        secondaryException
                );
            }
        }
    }
}