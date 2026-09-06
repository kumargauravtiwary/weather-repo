package com.example.weather.model;

public record ForecastResponse(

        Current current

) {

    public record Current(

            double temperature_2m,

            int relative_humidity_2m,

            double apparent_temperature,

            double wind_speed_10m,

            int weather_code

    ) {
    }
}