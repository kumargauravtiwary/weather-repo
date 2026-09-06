package com.example.weather.controller;

import com.example.weather.dto.WeatherResponse;
import com.example.weather.service.WeatherService;

import jakarta.validation.constraints.NotBlank;

import org.springframework.validation.annotation.Validated;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weather")
@Validated
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(
            WeatherService weatherService) {

        this.weatherService = weatherService;
    }

    @GetMapping
    public WeatherResponse getWeather(

            @RequestParam

            @NotBlank(
                    message = "city must not be blank"
            )

            String city) {

        return weatherService.getWeather(city);
    }
}