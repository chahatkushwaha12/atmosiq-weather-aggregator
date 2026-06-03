package com.xtechwala.AtmosIQ.controller;

import com.xtechwala.AtmosIQ.dto.WeatherResponse;
import com.xtechwala.AtmosIQ.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping
    public WeatherResponse getWeather(@RequestParam String city){
        return weatherService.getWeather(city);
    }

}
