package com.xtechwala.AtmosIQ.repository;

import com.xtechwala.AtmosIQ.entity.WeatherCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WeatherCacheRepository extends JpaRepository<WeatherCache, Long> {

    Optional<WeatherCache> findTopByCityOrderByFetchedAtDesc(String city);

}
