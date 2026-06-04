package com.xtechwala.AtmosIQ.service;

import com.xtechwala.AtmosIQ.dto.WeatherResponse;

public interface WeatherClient {

    WeatherResponse fetchWeather(String city);

    String getProviderName();

}
