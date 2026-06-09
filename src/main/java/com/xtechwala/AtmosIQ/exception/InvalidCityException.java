package com.xtechwala.AtmosIQ.exception;

public class InvalidCityException extends RuntimeException {
    public InvalidCityException(String city) {
        super("Invalid city name: '" + city + "'");
    }
}
