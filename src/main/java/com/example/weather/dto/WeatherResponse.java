package com.example.weather.dto;

public record WeatherResponse(

        String city,

        String country,

        double latitude,

        double longitude,

        double temperatureCelsius,

        double feelsLikeCelsius,

        int humidityPercent,

        double windSpeedKmh,

        String description

) {
}