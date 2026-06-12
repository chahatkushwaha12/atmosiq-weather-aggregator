package com.xtechwala.AtmosIQ.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "forecast_cache",
        uniqueConstraints = @UniqueConstraint(columnNames = {"city", "forecast_date"})
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ForecastCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "city", length = 100, nullable = false)
    String city;

    @Column(name = "forecast_date", nullable = false)
    LocalDate forecastDate;

    @Column(name = "max_temp")
    Double maxTemp;

    @Column(name = "min_temp")
    Double minTemp;

    @Column(name = "avg_temp")
    Double avgTemp;

    @Column(name = "max_wind_speed")
    Double maxWindSpeed;

    @Column(name = "avg_humidity")
    Integer avgHumidity;

    @Column(name = "chance_of_rain")
    Integer chanceOfRain;

    @Column(name = "description", length = 255)
    String description;

    @Column(name = "icon_url", length = 255)
    String iconUrl;

    @Column(name = "source", length = 50, nullable = false)
    String source;

    @Column(name = "fetched_at", nullable = false)
    LocalDateTime fetchedAt;
}