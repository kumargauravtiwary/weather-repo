package com.example.weather.service;

import com.example.weather.client.OpenMeteoClient;
import com.example.weather.dto.WeatherResponse;
import com.example.weather.model.ForecastResponse;
import com.example.weather.model.GeocodingResponse;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class WeatherService {

    private final OpenMeteoClient client;


    public WeatherService(
            OpenMeteoClient client
    ) {

        this.client = client;
    }


    @Cacheable(
            value = "weather",
            key = "#city.trim().toLowerCase()"
    )
    public WeatherResponse getWeather(
            String city
    ) {

        System.out.println(
                "Calling external weather APIs for: "
                        + city
        );


        // Step 1
        // Convert city -> latitude/longitude

        GeocodingResponse.Location location =
                client.geocode(city);


        // Step 2
        // Get weather

        ForecastResponse forecast =
                client.getForecast(

                        location.latitude(),

                        location.longitude()
                );


        if (

                forecast == null

                || forecast.current() == null

        ) {

            throw new IllegalStateException(
                    "Weather response did not contain current conditions"
            );
        }


        ForecastResponse.Current current =
                forecast.current();


        // Step 3
        // Convert provider response into our DTO

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


    private String weatherDescription(
            int code
    ) {

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