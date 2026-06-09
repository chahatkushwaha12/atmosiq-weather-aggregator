package com.xtechwala.AtmosIQ.controller;

import com.xtechwala.AtmosIQ.dto.WeatherResponse;
import com.xtechwala.AtmosIQ.service.WeatherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
@Validated
@Tag(name = "Weather", description = "Fetch real-time weather data for any city")
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping
    @Operation(
            summary = "Get weather by city",
            description = "Returns current weather data. Served from in-memory cache ->DB cache -> live API with automatic provider fallback."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Weather data returned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid city name"),
            @ApiResponse(responseCode = "404", description = "City not found"),
            @ApiResponse(responseCode = "503", description = "All weather providers are down")
    })
    public WeatherResponse getWeather(
            @RequestParam @NotBlank(message = "City name cannot be blank") @Size(max = 100)
            @Pattern(regexp = "^[a-zA-Z\\s\\-]+$", message = "City name must contain only letters")
            String city) {
        return weatherService.getWeather(city.trim());
    }
}
