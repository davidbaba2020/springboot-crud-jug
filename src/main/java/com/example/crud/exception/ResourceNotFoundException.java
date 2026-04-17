package com.example.crud.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * ════════════════════════════════════════════════════════════
 *  @ResponseStatus
 * ════════════════════════════════════════════════════════════
 *
 * Ties an HTTP status code to this exception class.
 * When Spring MVC catches this exception (and it's not handled by
 * a @ExceptionHandler), it automatically sets the HTTP status
 * to the specified code.
 *
 * value = HttpStatus.NOT_FOUND → response will be 404
 * reason = "..." → sets the response reason phrase
 *
 * Note: In our app, GlobalExceptionHandler handles this more
 * gracefully by returning a structured JSON error body.
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s : '%s'", resourceName, fieldName, fieldValue));
    }
}
