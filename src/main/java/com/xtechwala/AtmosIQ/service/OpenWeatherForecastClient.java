package com.xtechwala.AtmosIQ.service;

import com.xtechwala.AtmosIQ.dto.ForecastDay;
import com.xtechwala.AtmosIQ.dto.ForecastResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Fallback forecast provider using OpenWeatherMap /forecast endpoint (3-hour intervals).
 * Free tier supports up to 5 days. Entries are aggregated per calendar day.
 *
 * Sample URL:
 * https://api.openweathermap.org/data/2.5/forecast?q=Delhi&appid=KEY&units=metric&cnt=40
 */
@Service
@Slf4j
public class OpenWeatherForecastClient implements ForecastClient {

    public static final String PROVIDER = "OpenWeatherMap";

    // OpenWeather free tier caps at 5 days; 40 slots = 5 days x 8 slots/day
    private static final int OWN_MAX_SLOTS = 40;

    private final WebClient webClient;
    private final String apiKey;

    @Value("${weather.openweathermap.forecast-url:https://api.openweathermap.org/data/2.5/forecast}")
    private String forecastUrl;

    public OpenWeatherForecastClient(WebClient webClient,
                                     @Value("${weather.openweathermap.api-key}") String apiKey) {
        this.webClient = webClient;
        this.apiKey = apiKey;
    }

    @Override
    public ForecastResponse fetchForecast(String city, int days) {
        log.info("[{}] Fetching {}-day forecast for city='{}'", PROVIDER, days, city);

        try {
            JsonNode root = webClient.get()
                    .uri(forecastUrl, builder -> builder
                            .queryParam("q", city)
                            .queryParam("appid", apiKey)
                            .queryParam("units", "metric")
                            .queryParam("cnt", OWN_MAX_SLOTS)
                            .build())
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (root == null) {
                throw new RuntimeException("Empty response received");
            }

            String cod = root.path("cod").asText();
            if (!"200".equals(cod)) {
                String msg = root.path("message").asText("Unknown error");
                log.warn("[{}] API returned cod={}: {}", PROVIDER, cod, msg);
                throw new RuntimeException("OWM error cod=" + cod + ": " + msg);
            }

            List<ForecastDay> forecastDays = aggregateToDailyForecasts(root, days);
            log.info("[{}] Successfully aggregated {}-day forecast for '{}'", PROVIDER, forecastDays.size(), city);

            return ForecastResponse.builder()
                    .city(city)
                    .source(PROVIDER)
                    .fetchedAt(LocalDateTime.now())
                    .cached(false)
                    .forecast(forecastDays)
                    .build();

        } catch (Exception ex) {
            log.error("[{}] Forecast API failed for city='{}': {}", PROVIDER, city, ex.getMessage());
            throw new RuntimeException(PROVIDER + " forecast failed", ex);
        }
    }

    private List<ForecastDay> aggregateToDailyForecasts(JsonNode root, int requestedDays) {
        Map<LocalDate, List<JsonNode>> byDate = new LinkedHashMap<>();

        JsonNode list = root.path("list");
        for (JsonNode slot : list) {
            long dt = slot.path("dt").asLong();
            LocalDate date = Instant.ofEpochSecond(dt)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            byDate.computeIfAbsent(date, d -> new ArrayList<>()).add(slot);
        }

        List<ForecastDay> result = new ArrayList<>();
        int cnt = 0;

        for (Map.Entry<LocalDate, List<JsonNode>> entry : byDate.entrySet()) {
            if (cnt >= requestedDays) break;

            LocalDate date = entry.getKey();
            List<JsonNode> slots = entry.getValue();

            // Temperature stats
            DoubleSummaryStatistics tempStats = slots.stream()
                    .mapToDouble(s -> s.path("main").path("temp").asDouble())
                    .summaryStatistics();

            // Max wind speed (m/s -> km/h)
            double maxWind = slots.stream()
                    .mapToDouble(s -> s.path("wind").path("speed").asDouble())
                    .max()
                    .orElse(0.0);
            double maxWindKph = maxWind * 3.6;

            // Average humidity
            double avgHumidity = slots.stream()
                    .mapToInt(s -> s.path("main").path("humidity").asInt())
                    .average()
                    .orElse(0.0);

            // Most frequent weather description
            String description = slots.stream()
                    .map(s -> s.path("weather").get(0).path("description").asText("N/A"))
                    .collect(java.util.stream.Collectors.groupingBy(
                            d -> d, java.util.stream.Collectors.counting()))
                    .entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("N/A");

            // Chance of rain: max pop (0.0-1.0) across all slots * 100
            int chanceOfRain = (int) Math.round(
                    slots.stream()
                            .mapToDouble(s -> s.path("pop").asDouble(0.0))
                            .max()
                            .orElse(0.0) * 100
            );

            // Icon: pick slot closest to midday for most representative icon
            String iconCode = slots.stream()
                    .min(Comparator.comparingLong(s -> {
                        long dt = s.path("dt").asLong();
                        long hour = Instant.ofEpochSecond(dt)
                                .atZone(ZoneId.systemDefault())
                                .getHour();
                        return Math.abs(hour - 12);
                    }))
                    .map(s -> s.path("weather").path(0).path("icon").asText(null))
                    .orElse(null);

            String iconUrl = iconCode != null
                    ? "https://openweathermap.org/img/wn/" + iconCode + "@2x.png"
                    : null;

            result.add(ForecastDay.builder()
                    .date(date)
                    .maxTemp(Math.round(tempStats.getMax() * 10.0) / 10.0)
                    .minTemp(Math.round(tempStats.getMin() * 10.0) / 10.0)
                    .avgTemp(Math.round(tempStats.getAverage() * 10.0) / 10.0)
                    .maxWindSpeed(Math.round(maxWindKph * 10.0) / 10.0)
                    .avgHumidity((int) Math.round(avgHumidity))
                    .chanceOfRain(chanceOfRain)
                    .description(description)
                    .iconUrl(iconUrl)
                    .build());

            cnt++;
        }

        return result;
    }

    @Override
    public String getProviderName() {
        return PROVIDER;
    }
}