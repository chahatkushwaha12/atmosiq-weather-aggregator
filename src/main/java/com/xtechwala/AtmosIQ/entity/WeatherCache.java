package com.xtechwala.AtmosIQ.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "weather_cache")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class WeatherCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "city", length = 100, nullable = false)
    private String city;

    @Column(name = "temperature")
    private Double temperature;

    @Column(name = "humidity")
    private Integer humidity;

    @Column(name = "wind_speed")
    private Double windSpeed;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "source", length = 50, nullable = false)
    private String source;

    @Column(name = "fetched_at", nullable = false)
    private LocalDateTime fetchedAt;

}
