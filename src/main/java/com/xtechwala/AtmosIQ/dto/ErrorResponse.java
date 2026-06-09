package com.xtechwala.AtmosIQ.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // Agar koi field null hai to JSON response me nhi acayegi
@Schema(description = "Standard error response")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ErrorResponse {

    @Schema(description = "HTTP status code", example = "403")
    int status;

    @Schema(description = "Error message", example = "City not found: 'XYZ'")
    String message;

    @Schema(description = "Timestamp of the error")
    LocalDateTime dateTime;

}
