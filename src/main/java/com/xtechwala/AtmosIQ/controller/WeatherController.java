package com.xtechwala.AtmosIQ.controller;

import com.xtechwala.AtmosIQ.dto.WeatherResponse;
import com.xtechwala.AtmosIQ.service.WeatherService;
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
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping
    public WeatherResponse getWeather(
            @RequestParam @NotBlank(message = "City name cannot be blank") @Size(max = 100)
            @Pattern(regexp = "^[a-zA-Z\\s\\-]+$", message = "City name must contain only letters")
            String city) {
        return weatherService.getWeather(city.trim());
    }
}
