package com.xtechwala.AtmosIQ.service;

import com.xtechwala.AtmosIQ.dto.ForecastDay;
import com.xtechwala.AtmosIQ.dto.ForecastResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class WeatherApiForecastClient implements ForecastClient {

    private static final String PROVIDER = "WeatherApi";

    private final WebClient webClient;
    private final String apiKey;

    // e.g. https://api.weatherapi.com/v1/forecast.json
    @Value("${weather.weatherapi.forecast-url:https://api.weatherapi.com/v1/forecast.json}")
    private String forecastUrl;

    public WeatherApiForecastClient(WebClient webClient,
                                    @Value("${weather.weatherapi.api-key}") String apiKey){
        this.webClient = webClient;
        this.apiKey = apiKey;
    }

    @Override
    public ForecastResponse fetchForecast(String city, int days) {
        log.info("[{}] Fetching {}-day forecast for city='{}'", PROVIDER, days, city);

        try{
            JsonNode root = webClient.get()
                    .uri(forecastUrl, builder -> builder
                            .queryParam("key", apiKey)
                            .queryParam("q", city)
                            .queryParam("days", days)
                            .queryParam("aqi", "no")
                            .queryParam("alerts", "no")
                            .build())
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if(root == null){
                throw new RuntimeException("Empty response received");
            }

            // WeatherAPI returns error code inside JSON body (not HTTP error) for bad cities
            if(root.has("error")){
                int errorCode = root.path("error").path("code").asInt();
                String errorMsg = root.path("error").path("message").asText("Unknown error");
                log.warn("[{}] API returned error {}: {}", PROVIDER, errorCode, errorMsg);

                throw new RuntimeException("WeatherAPI error "+ errorCode+": "+errorMsg);
            }

            List<ForecastDay> forecastDays = parseForecastDays(root);
            log.info("[{}] Successfully fetched {}-day forecast for '{}'", PROVIDER, forecastDays.size(), city);

            return ForecastResponse.builder()
                    .city(city)
                    .source(PROVIDER)
                    .fetchedAt(LocalDateTime.now())
                    .cached(false)
                    .forecast(forecastDays)
                    .build();

        }catch(Exception ex){
            log.error("[{}] Forecast API failed for city='{}': {}", PROVIDER, city, ex.getMessage());
            throw new RuntimeException(PROVIDER+" forecast failed", ex);
        }
    }

    private List<ForecastDay> parseForecastDays(JsonNode root) {
        List<ForecastDay> result = new ArrayList<>();

        JsonNode forecastNode = root.path("forecast").path("forecastDay");
        if(!forecastNode.isArray()){
            return result;
        }

        for(JsonNode dayNode : forecastNode){
            String dataStr = dayNode.path("date").asText();
            JsonNode day = dayNode.path("day");
            JsonNode condition = day.path("condition");

            String iconUrl = condition.path("icon").asText(null);
            // WeatherAPI returns protocol-relative URLs like //cdn.weatherapi.com/...

            if(iconUrl != null && iconUrl.startsWith("//")){
                iconUrl = "https:"+iconUrl;
            }

            ForecastDay forecastDay = ForecastDay.builder()
                    .date(LocalDate.parse(dataStr))
                    .maxTemp(day.path("maxtemp_c").asDouble())
                    .minTemp(day.path("mintemp_c").asDouble())
                    .avgTemp(day.path("avgtemp_c").asDouble())
                    .maxWindSpeed(day.path("maxwind_kph").asDouble())
                    .avgHumidity(day.path("avghumidity").asInt())
                    .chanceOfRain(day.path("daily_chance_of_rain").asInt())
                    .description(condition.path("text").asText("N/A"))
                    .iconUrl(iconUrl)
                    .build();

            result.add(forecastDay);
        }
        return result;
    }

    @Override
    public String getProviderName() {
        return PROVIDER;
    }
}
