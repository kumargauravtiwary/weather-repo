package com.example.weather.model;

public record WeatherApiResponse(

        Location location,

        Current current

) {

    public record Location(

            String name,

            String country,

            double lat,

            double lon

    ) {
    }

    public record Current(

            double temp_c,

            double feelslike_c,

            int humidity,

            double wind_kph,

            Condition condition

    ) {
    }

    public record Condition(

            String text,

            int code

    ) {
    }
}