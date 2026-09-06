package com.example.weather.client;

import com.example.weather.exception.ExternalWeatherApiException;
import com.example.weather.model.WeatherApiResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import org.springframework.web.reactive.function.client.WebClient;

@Component
public class WeatherApiClient {

    private final WebClient webClient;

    private final String url;

    private final String apiKey;

    public WeatherApiClient(

            WebClient webClient,

            @Value(
                "${weather.providers.weather-api.url}"
            )
            String url,

            @Value(
                "${weather.providers.weather-api.api-key}"
            )
            String apiKey) {

        this.webClient = webClient;

        this.url = url;

        this.apiKey = apiKey;
    }

    public WeatherApiResponse getWeather(
            String city) {

        try {

            return webClient

                    .get()

                    .uri(

                            url
                                    + "?key={key}"
                                    + "&q={city}"
                                    + "&aqi=no",

                            apiKey,

                            city

                    )

                    .retrieve()

                    .bodyToMono(
                            WeatherApiResponse.class
                    )

                    .block();

        } catch (Exception ex) {

            throw new ExternalWeatherApiException(
                    "WeatherAPI provider failed",
                    ex
            );
        }
    }
}