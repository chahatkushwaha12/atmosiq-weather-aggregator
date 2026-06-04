package com.xtechwala.AtmosIQ.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Unified weather response from aggregated providers")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WeatherResponse{

    @Schema(description = "City name", example = "Lucknow")
    String city;

    @Schema(description = "Temperature in Celsius", example = "34.5")
    Double temperature;

    @Schema(description = "Humidity percentage", example = "62")
    Integer humidity;

    @Schema(description = "Wind speed in km/h", example = "12.3")
    Double windSpeed;

    @Schema(description = "Weather description", example = "Clear sky")
    String description;

    @Schema(description = "Data source provider", example = "OpenWeatherMap")
    String source;

    @Schema(description = "Timestamp when data was fetched")
    LocalDateTime fetchedAt;

    @Schema(description = "Whether data was served from cache", example = "true")
    Boolean cached;
}
