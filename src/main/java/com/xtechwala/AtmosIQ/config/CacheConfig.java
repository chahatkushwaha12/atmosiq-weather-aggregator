package com.xtechwala.AtmosIQ.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Explicit Caffeine cache configuration.
 *
 * Two caches:
 *  - "weather"  : current conditions per city  — expires after 10 min
 *  - "forecast" : 7-day forecast per city       — expires after 3 hours
 *                 (forecasts change much more slowly than current conditions)
 */
@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(){
        CaffeineCacheManager manager = new CaffeineCacheManager("weather", "forecast");

        // Per-entry TTL is handled by Spring's @CacheEvict in the schedulers,
        // but we add a safety-net maximum TTL so stale entries never persist forever.
        manager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(3, TimeUnit.HOURS)
                .maximumSize(200));

        return manager;
    }
}
