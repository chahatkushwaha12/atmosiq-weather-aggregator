package com.xtechwala.AtmosIQ.schedular;

import com.xtechwala.AtmosIQ.service.ForecastService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ForecastCacheSchedular {

    private final ForecastService forecastService;

    /*
     * Forecasts change much more slowly than current weather.
     * Refresh every 3 hours (10_800_000 ms) by default, configurable via property.
     *
     * We evict the entire in-memory cache so each city gets a clean re-fetch.
     */
    @Scheduled(fixedRateString = "${scheduler.forecast-refresh-interval-ms:10800000}")
    @CacheEvict(value = "forecast", allEntries = true, beforeInvocation = true)
    public void refreshForecastCities(){
        List<String> cities = forecastService.getCachedCities();

        if(cities.isEmpty()){
            log.info("[ForecastSchedular] No cached cities to refresh");
            return;
        }

        log.info("[ForecastSchedular] Failed to refresh forecast for '{}': {}", cities.size(), cities);

        for(String city : cities){
            try{
                forecastService.refreshForecastForCity(city);
            }catch(Exception ex){
                // Don't let one city failure abort the entire refresh cycle
                log.error("[ForecastSchedular] Failed to refresh forecast for '{}': {}", city, ex.getMessage());
            }
        }

        log.info("[ForecastSchedular] Forecast cycle complete.");
    }
}
