package com.xtechwala.AtmosIQ.schedular;

import com.xtechwala.AtmosIQ.dto.WeatherResponse;
import com.xtechwala.AtmosIQ.entity.WeatherCache;
import com.xtechwala.AtmosIQ.repository.WeatherCacheRepository;
import com.xtechwala.AtmosIQ.service.WeatherClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Scheduled(fixedRateString = "${scheduler.refresh-interval-ms:600000}")
    public void refreshCachedCities(){
        List<String> cities = weatherCacheRepository.findDistinctCities();

        if(cities.isEmpty()){
            log.info("[Schedular] No cached cities to refresh");
            return;
        }

        log.info("[Schedular] Refreshing weather for {} cities: {}", cities.size(), cities);

        for(String city : cities){
            refreshCity(city);
        }

        log.info("[Schedular] Refresh cycle complete.");
    }

    public void refreshCity(String city){
        for(WeatherClient client : weatherClients){
            try{
                WeatherResponse response = client.fetchWeather(city);
                if(response!=null){

                    Optional<WeatherCache> existing = weatherCacheRepository
                            .findTopByCityOrderByFetchedAtDesc(city);

                    WeatherCache cache;
                    if(existing.isPresent()){
                        cache = existing.get();
                    } else {
                        cache = new WeatherCache();
                    }

                    cache.setCity(response.getCity());
                    cache.setTemperature(response.getTemperature());
                    cache.setHumidity(response.getHumidity());
                    cache.setWindSpeed(response.getWindSpeed());
                    cache.setDescription(response.getDescription());
                    cache.setSource(response.getSource());
                    cache.setFetchedAt(LocalDateTime.now());

                    weatherCacheRepository.save(cache);
                    log.info("[Schedular] Refreshed '{}' via {}", city, client.getProviderName());
                }
            }catch(Exception ex){
                log.error("[Schedular] Failed to refresh '{}' via {}: {}", city, client.getProviderName(), ex.getMessage());
            }
        }
    }

}
