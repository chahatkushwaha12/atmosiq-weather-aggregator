package com.xtechwala.AtmosIQ.exception;

import com.xtechwala.AtmosIQ.dto.ErrorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 400 - Invalid city name (blank, null, special chars)
    @ExceptionHandler(InvalidCityException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidCity(InvalidCityException ex){
        log.warn("[Exception Handler] Invalid city: {}", ex.getMessage());
        return ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage())
                .dateTime(LocalDateTime.now())
                .build();
    }

    // 404 - City not found by weather API
    @ExceptionHandler(CityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleCityNotFound(CityNotFoundException ex){
        log.warn("[Exception Handler] City not found: {}", ex.getMessage());
        return ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .message(ex.getMessage())
                .dateTime(LocalDateTime.now())
                .build();
    }

    // 503 - All providers failed
    @ExceptionHandler(AllProvidorsDownException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ErrorResponse handleAllProvidersDown(AllProvidorsDownException ex){
        log.warn("[Exception Handler] All providers down: {}", ex.getMessage());
        return ErrorResponse.builder()
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .message(ex.getMessage())
                .dateTime(LocalDateTime.now())
                .build();
    }

    // 500 - DB error
    @ExceptionHandler(DatabaseException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleDatabaseException(DatabaseException ex){
        log.warn("[Exception Handler] DB error: {}", ex.getMessage());
        return ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message(ex.getMessage())
                .dateTime(LocalDateTime.now())
                .build();
    }

    // 500 - Unexpected fallback
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGenericException(Exception ex){
        log.warn("[Exception Handler] Unexpected error: {}", ex.getMessage());
        return ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message(ex.getMessage())
                .dateTime(LocalDateTime.now())
                .build();
    }
}
