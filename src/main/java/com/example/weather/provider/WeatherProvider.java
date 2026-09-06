package com.example.weather.provider;

import com.example.weather.dto.WeatherResponse;

public interface WeatherProvider {

    WeatherResponse getWeather(String city);
}