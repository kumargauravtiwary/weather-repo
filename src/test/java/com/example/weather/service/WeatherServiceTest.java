package com.example.weather.service;

import com.example.weather.client.OpenMeteoClient;

import com.example.weather.dto.WeatherResponse;

import com.example.weather.model.ForecastResponse;
import com.example.weather.model.GeocodingResponse;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WeatherServiceTest {


    @Test
    void shouldAggregateWeatherData() {

        OpenMeteoClient client =
                mock(OpenMeteoClient.class);


        when(
                client.geocode("Bengaluru")
        )

        .thenReturn(

                new GeocodingResponse.Location(

                        "Bengaluru",

                        12.9716,

                        77.5946,

                        "India"
                )
        );


        when(

                client.getForecast(
                        12.9716,
                        77.5946
                )

        )

        .thenReturn(

                new ForecastResponse(

                        new ForecastResponse.Current(

                                27.0,

                                65,

                                28.0,

                                8.5,

                                1
                        )
                )
        );


        WeatherService service =
                new WeatherService(client);


        WeatherResponse result =
                service.getWeather("Bengaluru");


        assertEquals(
                "Bengaluru",
                result.city()
        );


        assertEquals(
                27.0,
                result.temperatureCelsius()
        );


        assertEquals(
                65,
                result.humidityPercent()
        );


        verify(client)
                .geocode("Bengaluru");


        verify(client)
                .getForecast(
                        12.9716,
                        77.5946
                );
    }
}