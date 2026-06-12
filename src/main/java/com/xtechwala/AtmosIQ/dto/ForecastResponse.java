package com.xtechwala.AtmosIQ.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "7-day weather forecast response")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ForecastResponse {

    @Schema(description = "City name", example = "Varanasi")
    String city;

    @Schema(description = "Data source provider", example = "WeatherApi")
    String source;

    @Schema(description = "Timestamp when forecast was fetched")
    LocalDateTime fetchedAt;

    @Schema(description = "Weather data was served from cache", example = "false")
    Boolean cached;

    @Schema(description = "List of daily forecasts for the next 7 days")
    List<ForecastDay> forecast;

}
