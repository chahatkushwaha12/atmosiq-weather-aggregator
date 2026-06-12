package com.xtechwala.AtmosIQ.service;

import com.xtechwala.AtmosIQ.dto.ForecastResponse;

public interface ForecastClient {

    ForecastResponse fetchForecast(String city, int days);

    String getProviderName();

}
