package com.xtechwala.AtmosIQ.exception;

public class CityNotFoundException extends RuntimeException {
    public CityNotFoundException(String city) {
        super("City not found: '" + city + "'");
    }
}
