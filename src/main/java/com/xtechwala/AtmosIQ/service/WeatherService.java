package com.xtechwala.AtmosIQ.service;

import com.xtechwala.AtmosIQ.dto.WeatherResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.JsonNode;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherService {

    /*private final RestTemplate restTemplate;

    @Value("${weather.api.key}")
    private String apiKey;

    public WeatherResponse getWeather(String city) {

        String url = "https://api.openweathermap.org/data/2.5/weather?q="
                + city
                + "&appid="
                + apiKey
                + "&units=metric";
        System.out.println(url);
        JsonNode response = restTemplate.getForObject(url, JsonNode.class);

        System.out.println("===== Weather Data =====");
        System.out.println(response);
        System.out.println("========================");

        return new WeatherResponse(
                response.get("name").asText(),
                response.get("sys").get("country").asText(),
                response.get("main").get("temp").asDouble(),
                response.get("main").get("feels_like").asDouble(),
                response.get("main").get("humidity").asInt(),
                response.get("main").get("pressure").asInt(),
                response.get("weather").get(0).get("description").asText(),
                response.get("wind").get("speed").asDouble(),
                response.get("clouds").get("all").asInt()
        );
    }*/


    /*private final RestClient restClient;

    @Value("${weather.api.key}")
    String apiKey;

    public WeatherResponse getWeather(String city){

        String url = "https://api.openweathermap.org/data/2.5/weather?q="
                + city
                + "&appid="
                + apiKey
                + "&units=metric";
        System.out.println(url);
        JsonNode response = restClient.get()
                        .uri(url)
                        .retrieve()
                        .body(JsonNode.class);

        System.out.println("===== Weather Data =====");
        System.out.println(response);
        System.out.println("========================");

        return new WeatherResponse(
                response.path("name").asText(),
                response.path("sys").path("country").asText(),
                response.path("main").path("temp").asDouble(),
                response.path("main").path("feels_like").asDouble(),
                response.path("main").path("humidity").asInt(),
                response.path("main").path("pressure").asInt(),
                response.path("weather").path(0).path("description").asText(),
                response.path("wind").path("speed").asDouble(),
                response.path("clouds").path("all").asInt()
        );
    }*/

    private final WebClient webClient;

    @Value("${weather.api.key}")
    String apiKey;

    public WeatherResponse getWeather(String city){

        String url = "https://api.openweathermap.org/data/2.5/weather?q="
                + city
                + "&appid="
                + apiKey
                + "&units=metric";
        System.out.println(url);

        JsonNode response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        return new WeatherResponse(
                response.path("name").asText(),
                response.path("sys").path("country").asText(),
                response.path("main").path("temp").asDouble(),
                response.path("main").path("feels_like").asDouble(),
                response.path("main").path("humidity").asInt(),
                response.path("main").path("pressure").asInt(),
                response.path("weather").path(0).path("description").asText(),
                response.path("wind").path("speed").asDouble(),
                response.path("clouds").path("all").asInt()
        );
    }

}
