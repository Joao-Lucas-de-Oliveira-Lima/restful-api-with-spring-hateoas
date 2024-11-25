package edu.jl.springhateoas.dto.exception;

import java.util.Date;

public record ExceptionResponseDto(
        Date timestamp,
        String details,
        String message) {
}
