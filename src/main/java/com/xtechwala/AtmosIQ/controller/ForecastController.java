package com.xtechwala.AtmosIQ.controller;

import com.xtechwala.AtmosIQ.dto.ForecastResponse;
import com.xtechwala.AtmosIQ.service.ForecastService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/weather")
@RequiredArgsConstructor
@Validated
@Tag(name = "Weather", description = "Fetch real-time weather data for any city")
public class ForecastController {

    private final ForecastService forecastService;

    @GetMapping
    @Operation(
            summary = "Get 7-day weather forecast by city",
            description = "Returns a 7-day daily forecast including min/max/avg temperature, " +
                    "humidity, wind speed, and rain probability. " +
                    "Served from in-memory cache → DB cache → live API with automatic provider fallback. " +
                    "Primary provider: WeatherAPI.com. Fallback: OpenWeatherMap (5 days, no rain %)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Forecast data returned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid city name"),
            @ApiResponse(responseCode = "404", description = "City not found"),
            @ApiResponse(responseCode = "503", description = "All weather providers are down")
    })
    public ForecastResponse getForecast(
            @RequestParam @NotBlank(message = "City can not be blank") @Size(max = 100)
            @Pattern(regexp = "^[a-zA-Z\\s\\-]+$", message = "City name must contain only letters")
            String city
    ){
        return forecastService.getForecast(city.trim());
    }
}
