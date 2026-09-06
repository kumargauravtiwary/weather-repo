package com.example.weather.client;

import com.example.weather.exception.ExternalWeatherApiException;
import com.example.weather.exception.LocationNotFoundException;

import com.example.weather.model.ForecastResponse;
import com.example.weather.model.GeocodingResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import org.springframework.web.reactive.function.client.WebClient;

@Component
public class OpenMeteoClient {

    private final WebClient webClient;

    private final String geocodingUrl;

    private final String forecastUrl;


    public OpenMeteoClient(

            WebClient webClient,

            @Value("${weather.open-meteo.geocoding-url}")
            String geocodingUrl,

            @Value("${weather.open-meteo.forecast-url}")
            String forecastUrl

    ) {

        this.webClient = webClient;

        this.geocodingUrl = geocodingUrl;

        this.forecastUrl = forecastUrl;
    }


    public GeocodingResponse.Location geocode(
            String city
    ) {

        try {

            GeocodingResponse response =

                    webClient

                            .get()

                            .uri(

                                    geocodingUrl
                                            + "/v1/search"
                                            + "?name={city}"
                                            + "&count=1"
                                            + "&language=en"
                                            + "&format=json",

                                    city

                            )

                            .retrieve()

                            .bodyToMono(
                                    GeocodingResponse.class
                            )

                            .block();


            if (

                    response == null

                    || response.results() == null

                    || response.results().isEmpty()

            ) {

                throw new LocationNotFoundException(
                        "City not found: " + city
                );
            }


            return response.results().get(0);

        }

        catch (LocationNotFoundException ex) {

            throw ex;
        }

        catch (Exception ex) {

            throw new ExternalWeatherApiException(
                    "Geocoding API failed",
                    ex
            );
        }
    }


    public ForecastResponse getForecast(

            double latitude,

            double longitude

    ) {

        try {

            return webClient

                    .get()

                    .uri(

                            forecastUrl
                                    + "/v1/forecast"
                                    + "?latitude={lat}"
                                    + "&longitude={lon}"
                                    + "&current="
                                    + "temperature_2m,"
                                    + "relative_humidity_2m,"
                                    + "apparent_temperature,"
                                    + "wind_speed_10m,"
                                    + "weather_code"
                                    + "&timezone=auto",

                            latitude,

                            longitude

                    )

                    .retrieve()

                    .bodyToMono(
                            ForecastResponse.class
                    )

                    .block();

        }

        catch (Exception ex) {

            throw new ExternalWeatherApiException(
                    "Forecast API failed",
                    ex
            );
        }
    }
}