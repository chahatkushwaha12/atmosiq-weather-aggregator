package com.xtechwala.AtmosIQ.service;

import com.xtechwala.AtmosIQ.dto.WeatherResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.JsonNode;

@Service
@Slf4j
public class WeatherApiClient implements WeatherClient{

    private static final String PROVIDER = "WeatherApi";

    private final WebClient webClient;
    private final String apiKey;
    private final String baseUrl;

    public WeatherApiClient(WebClient webClient,
                            @Value("${weather.weatherapi.api-key}") String apiKey,
                            @Value("${weather.weatherapi.base-url}") String baseUrl){
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
                            .queryParam("key", apiKey)
                            .queryParam("q", city)
                            .queryParam("api", "no")
                            .build())
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if(root == null){
                throw new RuntimeException("Empty response received");
            }

            JsonNode current = root.path("current");

            String description = current
                    .path("condition")
                    .path("text")
                    .asText("N/A");

            log.info("[{}] Successfully fetched for '{}",PROVIDER, city);

            return WeatherResponse.builder()
                    .city(city)
                    .temperature(current.path("temp_c").asDouble())
                    .humidity(current.path("humidity").asInt())
                    .windSpeed(current.path("wind_kph").asDouble())
                    .description(description)
                    .source(PROVIDER)
                    .cached(false)
                    .build();
        }catch(Exception ex){
            log.error("[{}] API failed for city='{}':{}",PROVIDER, city, ex.getMessage());
            throw new RuntimeException(PROVIDER, ex);
        }
    }

    @Override
    public String getProviderName() {
        return PROVIDER;
    }
}
