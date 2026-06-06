package com.xtechwala.AtmosIQ.schedular;

import com.xtechwala.AtmosIQ.dto.WeatherResponse;
import com.xtechwala.AtmosIQ.entity.WeatherCache;
import com.xtechwala.AtmosIQ.repository.WeatherCacheRepository;
import com.xtechwala.AtmosIQ.service.WeatherClient;
import com.xtechwala.AtmosIQ.service.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class WeatherCacheSchedular {

    private final List<WeatherClient> weatherClients;
    private final WeatherCacheRepository weatherCacheRepository;

    private final WeatherService weatherService;

    @Scheduled(fixedRateString = "${scheduler.refresh-interval-ms:600000}")
    @CacheEvict(value = "weather", allEntries = true, beforeInvocation = true)
    public void refreshCachedCities(){
        List<String> cities = weatherCacheRepository.findDistinctCities();

        if(cities.isEmpty()){
            log.info("[Schedular] No cached cities to refresh");
            return;
        }

        log.info("[Schedular] Refreshing weather for {} cities: {}", cities.size(), cities);
        for(String city : cities){
            weatherService.getWeather(city);
        }
        log.info("[Schedular] Refresh cycle complete.");
    }

    /*@Scheduled(fixedRateString = "${scheduler.refresh-interval-ms:600000}")
    @CacheEvict(value = "weather", allEntries = true, beforeInvocation = true)
    public void refreshWeatherCache(){
        List<String> cities = weatherCacheRepository.findDistinctCities();
        for(String city : cities){
            weatherService.getWeather(city);
        }
        log.info("Weather cache refreshed");
    }*/

}
