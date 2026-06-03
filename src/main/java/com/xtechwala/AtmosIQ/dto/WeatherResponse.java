package com.xtechwala.AtmosIQ.dto;

public record WeatherResponse(
        String city,
        String country,
        double temperature,
        double feelsLike,
        int humidity,
        int pressure,
        String weather,
        double windSpeed,
        int cloudiness
) {
}
