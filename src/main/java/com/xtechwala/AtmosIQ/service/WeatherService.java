package com.xtechwala.AtmosIQ.service;

import com.xtechwala.AtmosIQ.dto.WeatherResponse;
import com.xtechwala.AtmosIQ.entity.WeatherCache;
import com.xtechwala.AtmosIQ.repository.WeatherCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherService {

    private final List<WeatherClient> weatherClients;
    private final WeatherCacheRepository weatherCacheRepository;

    public WeatherResponse getWeather(String city){
        return fetchFromProviders(city);
    }


    private WeatherResponse fetchFromProviders(String city){
        WeatherResponse response = null;
        for(WeatherClient weatherClient : weatherClients) {
            try {
                response = weatherClient.fetchWeather(city);
                if(response != null){
                    saveToDatabase(response);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return response;
    }

    private void saveToDatabase(WeatherResponse response){
        try{
            WeatherCache savedCache = WeatherCache.builder()
                    .city(response.getCity())
                    .temperature(response.getTemperature())
                    .humidity(response.getHumidity())
                    .windSpeed(response.getWindSpeed())
                    .description(response.getDescription())
                    .source(response.getSource())
                    .fetchedAt(LocalDateTime.now())
                    .build();

            weatherCacheRepository.save(savedCache);
            log.info("Saved weather data for city='{}' from source='{}'",
                    response.getCity(), response.getSource());
        }catch(Exception ex){
            log.error("Failed to save weather data to DB: {}", ex.getMessage());
        }
    }

}
