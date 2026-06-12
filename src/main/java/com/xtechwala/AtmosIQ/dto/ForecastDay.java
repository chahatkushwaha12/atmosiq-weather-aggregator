package com.xtechwala.AtmosIQ.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Weather forecast for a single day")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ForecastDay {

    @Schema(description = "Forecast date", example = "2025-06-11")
    LocalDate date;

    @Schema(description = "Maximum temperature in Celsius", example = "38.5")
    Double maxTemp;

    @Schema(description = "Minimum temperature in Celsius", example = "27.2")
    Double minTemp;

    @Schema(description = "Average temperature in Celsius", example = "33.1")
    Double avgTemp;

    @Schema(description = "Maximum wind speed in km/h", example = "18.0")
    Double maxWindSpeed;

    @Schema(description = "Average humidity percentage", example = "55")
    Integer avgHumidity;

    @Schema(description = "Chance of rain percentage", example = "20")
    Integer chanceOfRain;

    @Schema(description = "Weather condition description", example = "Partly cloudy")
    String description;

    @Schema(description = "Weather condition icon URL")
    String iconUrl;
}
