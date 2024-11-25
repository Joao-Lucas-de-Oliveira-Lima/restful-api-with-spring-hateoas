package edu.jl.springhateoas.exception;

import edu.jl.springhateoas.dto.exception.ExceptionResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Date;

@RestController
@ControllerAdvice
public class CustomizedResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponseDto> handlerException(WebRequest webRequest, Exception exception) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildResponseDto(webRequest, exception));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ExceptionResponseDto> handlerResourceNotFoundException(WebRequest webRequest, ResourceNotFoundException resourceNotFoundException) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(buildResponseDto(webRequest, resourceNotFoundException));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ExceptionResponseDto> handlerBadRequestException(WebRequest webRequest, BadRequestException badRequestException) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(buildResponseDto(webRequest, badRequestException));
    }

    private ExceptionResponseDto buildResponseDto(WebRequest webRequest, Exception exception){
        return new ExceptionResponseDto(new Date(), webRequest.getDescription(false), exception.getMessage());
    }
}
