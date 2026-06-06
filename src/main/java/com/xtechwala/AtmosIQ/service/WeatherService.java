package com.xtechwala.AtmosIQ.service;

import com.xtechwala.AtmosIQ.dto.WeatherResponse;
import com.xtechwala.AtmosIQ.entity.WeatherCache;
import com.xtechwala.AtmosIQ.repository.WeatherCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherService {

    private final List<WeatherClient> weatherClients;
    private final WeatherCacheRepository weatherCacheRepository;

    @Value("${scheduler.refresh-interval-ms:60000}")
    private long refreshIntervalMs;

    @Cacheable(value = "weather", key = "#city")
    public WeatherResponse getWeather(String city){
        Optional<WeatherCache> latestWeatherInDB = weatherCacheRepository
                .findTopByCityOrderByFetchedAtDesc(city);

        if(latestWeatherInDB.isPresent() && isRefreshNotRequired(latestWeatherInDB.get())){
            return toResponse(latestWeatherInDB.get());
        }
        return fetchFromProviders(city);
    }

    private boolean isRefreshNotRequired(WeatherCache cache){
        Duration time = Duration.between(cache.getFetchedAt(), LocalDateTime.now());
        return time.toMillis() < refreshIntervalMs;
    }

    private WeatherResponse fetchFromProviders(String city){
        WeatherResponse response = null;
        for(WeatherClient weatherClient : weatherClients) {
            try {
                response = weatherClient.fetchWeather(city);
                if(response != null){
                    upsertToDatabase(response);
                }
            } catch (Exception e) {
                log.error("Provider {} failed for city {}: {}",
                        weatherClient.getProviderName(), city, e.getStackTrace());
            }
        }
        return response;
    }

    private void upsertToDatabase(WeatherResponse response) {
        try {
            Optional<WeatherCache> existing = weatherCacheRepository
                    .findTopByCityOrderByFetchedAtDesc(response.getCity());

            WeatherCache cache;
            if (existing.isPresent()) {
                cache = existing.get();  // same row update
            } else {
                cache = new WeatherCache();  // pehli baar insert
            }

            cache.setCity(response.getCity());
            cache.setTemperature(response.getTemperature());
            cache.setHumidity(response.getHumidity());
            cache.setWindSpeed(response.getWindSpeed());
            cache.setDescription(response.getDescription());
            cache.setSource(response.getSource());
            cache.setFetchedAt(LocalDateTime.now());

            weatherCacheRepository.save(cache);
        } catch (Exception ex) {
            log.error("Failed to upsert weather data to DB: {}", ex.getMessage());
        }
    }

    private WeatherResponse toResponse(WeatherCache cache){
        return WeatherResponse.builder()
                .city(cache.getCity())
                .temperature(cache.getTemperature())
                .humidity(cache.getHumidity())
                .windSpeed(cache.getWindSpeed())
                .description(cache.getDescription())
                .source(cache.getSource())
                .fetchedAt(cache.getFetchedAt())
                .cached(true)
                .build();
    }

}
