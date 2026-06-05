package com.xtechwala.AtmosIQ.repository;

import com.xtechwala.AtmosIQ.entity.WeatherCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WeatherCacheRepository extends JpaRepository<WeatherCache, Long> {

    Optional<WeatherCache> findTopByCityOrderByFetchedAtDesc(String city);

//  Scheduler ko DB se all cities ka list chahiye jinhein refresh karna hai.
    @Query("SELECT DISTINCT w.city FROM WeatherCache w")
    List<String> findDistinctCities();

}
