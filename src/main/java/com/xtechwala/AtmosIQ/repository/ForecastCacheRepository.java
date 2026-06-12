package com.xtechwala.AtmosIQ.repository;

import com.xtechwala.AtmosIQ.entity.ForecastCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ForecastCacheRepository extends JpaRepository<ForecastCache, Long> {

    /* Fetch all forecast records of a city in date order. Example: Varanasi -> 11 Jun, 12 Jun, 13 Jun ... */
    List<ForecastCache> findByCityOrderByForecastDateAsc(String city);

    /* Find forecast for one specific date of a city. Example: city = Varanasi & date = 2026-06-12
    Use case:
    Save karte waqt check karna: "Kya is date ka record already DB me hai?"
    Agar hai → update, Agar nahi hai → insert */
    Optional<ForecastCache> findByCityAndForecastDate(String city, LocalDate forecastDate);

    /* Fetch the most recently stored forecast row for a city. ORDER BY fetchedAt DESC LIMIT 1
    Use case: Freshness check ke liye.Agar latest data abhi bhi valid hai,
    to API call ki need nhi h */
    Optional<ForecastCache> findTopByCityOrderByFetchedAtDesc(String city);

    // Delete stale (past) forecast entries for a city
    @Modifying
    @Transactional
    @Query("DELETE FROM ForecastCache f WHERE f.city = :city AND f.forecastDate < :today")
    void deleteStaleEntries(String city, LocalDate today);

    // Used by scheduler to find all cities with cached forecasts
    @Query("SELECT DISTINCT f.city FROM ForecastCache f")
    List<String> findDistinctCities();
}
