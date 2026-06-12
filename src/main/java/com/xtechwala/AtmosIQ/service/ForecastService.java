package com.xtechwala.AtmosIQ.service;

import com.xtechwala.AtmosIQ.dto.ForecastDay;
import com.xtechwala.AtmosIQ.dto.ForecastResponse;
import com.xtechwala.AtmosIQ.entity.ForecastCache;
import com.xtechwala.AtmosIQ.exception.AllProvidorsDownException;
import com.xtechwala.AtmosIQ.exception.DatabaseException;
import com.xtechwala.AtmosIQ.repository.ForecastCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ForecastService {

    private static final int FORECAST_DAYS = 7;

    private final List<ForecastClient> forecastClients;
    private final ForecastCacheRepository forecastCacheRepository;

    @Value("${scheduler.refresh-interval-ms:60000}")
    private long refreshIntervalMs;

    /*Agar "forecast" cache me "Varanasi" key already hai —
    then method body does not execute . Spring direct cached result
    return kart hai. Most fast, zero DB call.*/
    @Cacheable(value = "forecast", key = "#city")
    public ForecastResponse getForecast(String city) {
        // Check DB cache first
        Optional<ForecastCache> latestForecastInDB = forecastCacheRepository
                .findTopByCityOrderByFetchedAtDesc(city);

        // Check kro ki database me is city ka koi forecast data hai ya nhi
        if (latestForecastInDB.isPresent() && isRefreshNotRequired(latestForecastInDB.get())) {
            // DB Cache Se Data Read Saare 7 days records le aata hai.
            List<ForecastCache> cachedData = forecastCacheRepository
                    .findByCityOrderByForecastDateAsc(city);


            List<ForecastDay> days = cachedData.stream()
                    /* Agar forecast date aaj se before nhi hai to record ko allow kro
                    Example: Today = 10 June, 8 June  -> remove, 10 June -> keep, 11 June -> keep */
                    .filter(forecastCache -> !forecastCache.getForecastDate().isBefore(LocalDate.now()))
                    // ForecastCache entity converted into DTO
                    .map(this::toForecastDay)
                    // Stream converted into list
                    .collect(Collectors.toList());

            if (!days.isEmpty()) {
                log.warn("[ForecastService] Serving cached forecast for '{}'", city);
                return ForecastResponse.builder()
                        .city(city)
                        .source(latestForecastInDB.get().getSource())
                        .fetchedAt(latestForecastInDB.get().getFetchedAt())
                        .cached(true)
                        .forecast(days)
                        .build();
            }
        }
        return fetchFromProviders(city);
    }

    private boolean isRefreshNotRequired(ForecastCache forecastCache) {
        Duration time = Duration.between(forecastCache.getFetchedAt(), LocalDateTime.now());
        return time.toMillis() < refreshIntervalMs;
    }

    private ForecastResponse fetchFromProviders(String city) {
        ForecastResponse response = null;

        for (ForecastClient forecastClient : forecastClients) {
            try {
                response = forecastClient.fetchForecast(city, FORECAST_DAYS);
                if (isValidResponse(response)) {
                    upsertToDatabase(response, city);
                    break;
                } else {
                    log.warn("[{}] Invalid forecast data, trying next provider...", forecastClient.getProviderName());
                }
            } catch (Exception ex) {
                log.error("[{}] Forecast failed for '{}': {}", forecastClient
                        .getProviderName(), city, ex.getMessage());
            }
        }

        if (response == null) {
            throw new AllProvidorsDownException();
        }
        return response;
    }

    @Transactional
    public void upsertToDatabase(ForecastResponse response, String city) {
        try {
            // Clean up past entries first
            forecastCacheRepository.deleteStaleEntries(city, LocalDate.now());

            if (response.getForecast() == null) {
                return;
            }

            for (ForecastDay day : response.getForecast()) {
                Optional<ForecastCache> existing = forecastCacheRepository
                        .findByCityAndForecastDate(city, day.getDate());

                ForecastCache cache = existing.orElse(ForecastCache.builder()
                        .city(city)
                        .forecastDate(day.getDate())
                        .build());

                cache.setMaxTemp(day.getMaxTemp());
                cache.setMinTemp(day.getMinTemp());
                cache.setAvgTemp(day.getAvgTemp());
                cache.setMaxWindSpeed(day.getMaxWindSpeed());
                cache.setAvgHumidity(day.getAvgHumidity());
                cache.setChanceOfRain(day.getChanceOfRain());
                cache.setDescription(day.getDescription());
                cache.setIconUrl(day.getIconUrl());
                cache.setSource(response.getSource());
                cache.setFetchedAt(LocalDateTime.now());

                forecastCacheRepository.save(cache);
            }

            log.info("[ForecastService] Upserted {} forecast days for '{}' from {}",
                    response.getForecast().size(), city, response.getSource());
        } catch (Exception ex) {
            log.error("[ForecastService] Failed to upsert forecast to DB: {}", ex.getMessage());
            throw new DatabaseException("Failed to save forecast data to database", ex);
        }
    }

    /**
     * Called by the scheduler to refresh forecast cache for all known cities.
     */
    @CacheEvict(value = "forecast", key = "#city")
    public void refreshForecastForCity(String city) {
        log.info("[ForecastService] Scheduler refreshing forecast for '{}'", city);
        fetchFromProviders(city);
    }

    public List<String> getCachedCities() {
        return forecastCacheRepository.findDistinctCities();
    }

    // Mappers
    private ForecastDay toForecastDay(ForecastCache cache) {
        return ForecastDay.builder()
                .date(cache.getForecastDate())
                .maxTemp(cache.getMaxTemp())
                .minTemp(cache.getMinTemp())
                .avgTemp(cache.getAvgTemp())
                .maxWindSpeed(cache.getMaxWindSpeed())
                .avgHumidity(cache.getAvgHumidity())
                .chanceOfRain(cache.getChanceOfRain())
                .description(cache.getDescription())
                .iconUrl(cache.getIconUrl())
                .build();
    }

    private boolean isValidResponse(ForecastResponse forecastResponse) {
        return forecastResponse != null
                && forecastResponse.getForecast() != null
                && !forecastResponse.getForecast().isEmpty();
    }
}