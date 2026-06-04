package com.xtechwala.AtmosIQ.service;

import com.xtechwala.AtmosIQ.dto.WeatherResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.JsonNode;

@Slf4j
@Service
public class OpenWeatherClient implements WeatherClient{

    private static final String PROVIDER = "OpenWeatherMap";

    private final WebClient webClient;
    private final String apiKey;
    private final String baseUrl;

    public OpenWeatherClient(WebClient webClient,
                             @Value("${weather.openweathermap.api-key}") String apiKey,
                             @Value("${weather.openweathermap.base-url}") String baseUrl){
        this.webClient = webClient;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    @Override
    public WeatherResponse fetchWeather(String city) {
        log.info("[{}] Fetching weather for city='{}'", PROVIDER, city);

        try{

            JsonNode root = webClient.get()
                    .uri(baseUrl, builder -> builder
                            .queryParam("q", city)
                            .queryParam("appid", apiKey)
                            .queryParam("units", "metric")
                            .build())
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if(root == null){
                throw new RuntimeException("Empty response received");
            }

            String description = root.path("weather")
                    .get(0)
                    .path("description")
                    .asText("N/A");

            log.info("[{}] Successfully fetched for '{}'", PROVIDER, city);

            return WeatherResponse.builder()
                    .city(city)
                    .temperature(root.path("main").path("temp").asDouble())
                    .humidity(root.path("main").path("humidity").asInt())
                    .windSpeed(root.path("wind").path("speed").asDouble())
                    .description(description)
                    .source(PROVIDER)
                    .cached(false)
                    .build();
        }catch(Exception ex){
            log.error("[{}] API call failed for city='{}': {}",
                    PROVIDER, city, ex.getMessage());

            throw new RuntimeException(PROVIDER, ex);
        }
    }

    @Override
    public String getProviderName() {
        return PROVIDER;
    }
}
