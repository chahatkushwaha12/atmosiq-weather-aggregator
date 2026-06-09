package com.xtechwala.AtmosIQ.service;

import com.xtechwala.AtmosIQ.dto.WeatherResponse;
import com.xtechwala.AtmosIQ.entity.WeatherCache;
import com.xtechwala.AtmosIQ.exception.AllProvidorsDownException;
import com.xtechwala.AtmosIQ.exception.DatabaseException;
import com.xtechwala.AtmosIQ.repository.WeatherCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    /*Spring automatically is list mein dono clients inject karta hai:
    weatherClients = [OpenWeatherClient, WeatherApiClient]*/
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
        for(WeatherClient weatherClient : weatherClients) { // pehle OpenWeather try karo
            try {
                response = weatherClient.fetchWeather(city);
                if(isValidResponse(response)){
                    upsertToDatabase(response);
                    break;
                }else{
                    log.warn("[{}] Invalid data received, trying next provider...", weatherClient.getProviderName());
                }
            } catch (Exception e) {
                log.error("Provider {} failed for city {}: {}",
                        weatherClient.getProviderName(), city, e.getMessage()); // fail hua toh next pe jao
            }
        }

        // All providers failed or returned invalid data
        if(response == null){
            throw new AllProvidorsDownException();
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
            throw new DatabaseException("Failed to save weather data to database", ex);
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

    private boolean isValidResponse(WeatherResponse response){
        return response != null
                && response.getTemperature() >= -90 // world min temperature
                && response.getTemperature() <= 60 // world max temperature
                && response.getHumidity() >= 0
                && response.getHumidity() <= 100
                && response.getDescription() != null
                && !response.getDescription().isBlank();
    }
}

/*
Request aai city="Delhi"
        ↓
Cache fresh hai?  →  YES  →  Cache se return karo
        ↓ NO
OpenWeatherClient.fetchWeather("Delhi")
        ↓
Success?  →  YES  →  DB save karo, return karo
        ↓ NO (API down / wrong key)
WeatherApiClient.fetchWeather("Delhi")
        ↓
Success?  →  YES  →  DB save karo, return karo
        ↓ NO
null return hoga

Normal case → sirf OpenWeatherClient call hoga, WeatherApiClient call hi nahi hoga
OpenWeather fail hua (API down, key expired, limit exceed) → tab automatically WeatherApiClient pe fallback ho jaega
Ye failover/fallback pattern hai — dono ek saath call nhi hote */