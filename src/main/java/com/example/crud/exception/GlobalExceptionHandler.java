package com.example.crud.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * ════════════════════════════════════════════════════════════
 *  @RestControllerAdvice
 * ════════════════════════════════════════════════════════════
 *
 * A specialization of @ControllerAdvice that applies globally
 * across ALL controllers in the application.
 *
 * @ControllerAdvice = @Component + cross-cutting controller logic
 * @RestControllerAdvice = @ControllerAdvice + @ResponseBody
 *
 * This class acts as a GLOBAL exception handler — instead of
 * each controller having its own try/catch, all exceptions
 * bubble up here and are handled uniformly.
 *
 * Spring MVC will:
 *  1. Catch the exception from any controller method
 *  2. Find the matching @ExceptionHandler in this class
 *  3. Execute it and return its ResponseEntity as the HTTP response
 *
 * ════════════════════════════════════════════════════════════
 *  @ExceptionHandler
 * ════════════════════════════════════════════════════════════
 *
 * Maps a specific exception type to a handler method.
 * When an exception of that type is thrown from any controller,
 * Spring routes it here instead of returning a generic 500 error.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handles our custom 404 exception.
     * Returns a structured JSON error body instead of the default white-label error page.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            WebRequest request) {

        log.error("Resource not found: {}", ex.getMessage());

        Map<String, Object> errorBody = buildErrorBody(
                HttpStatus.NOT_FOUND.value(),
                "Resource Not Found",
                ex.getMessage(),
                request.getDescription(false)
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody);
    }

    /**
     * Handles validation errors thrown when @Valid fails.
     *
     * MethodArgumentNotValidException is thrown by Spring when
     * a @RequestBody fails Bean Validation (e.g., @NotBlank, @Size).
     *
     * We extract each field error and return them all in one response:
     * {
     *   "status": 400,
     *   "error": "Validation Failed",
     *   "fieldErrors": {
     *     "name": "must not be blank",
     *     "price": "must be greater than 0"
     *   }
     * }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex,
            WebRequest request) {

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        Map<String, Object> errorBody = buildErrorBody(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                "One or more fields have invalid values",
                request.getDescription(false)
        );
        errorBody.put("fieldErrors", fieldErrors);

        log.warn("Validation failed: {}", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody);
    }

    // ── Security exceptions ──────────────────────────────────────

    /** Bad username or password during login → 401 */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(
            BadCredentialsException ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(buildErrorBody(
                401, "Unauthorized", "Invalid username or password", request.getDescription(false)));
    }

    /** Account disabled → 401 */
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Map<String, Object>> handleDisabled(
            DisabledException ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(buildErrorBody(
                401, "Unauthorized", "Account is disabled", request.getDescription(false)));
    }

    /** Any other Spring Security auth failure → 401 */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthException(
            AuthenticationException ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(buildErrorBody(
                401, "Unauthorized", ex.getMessage(), request.getDescription(false)));
    }

    /** Insufficient role / @PreAuthorize failed → 403 */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(
            AccessDeniedException ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(buildErrorBody(
                403, "Forbidden", "You do not have permission to access this resource",
                request.getDescription(false)));
    }

    /** ResponseStatusException (thrown in controllers) */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(
            ResponseStatusException ex, WebRequest request) {
        return ResponseEntity.status(ex.getStatusCode()).body(buildErrorBody(
                ex.getStatusCode().value(), "Error", ex.getReason(), request.getDescription(false)));
    }

    /**
     * Catch-all handler for any unexpected exception.
     * Prevents stack traces from leaking to the client (a security concern).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex,
            WebRequest request) {

        log.error("Unexpected error: ", ex);

        Map<String, Object> errorBody = buildErrorBody(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred. Please try again later.",
                request.getDescription(false)
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody);
    }

    private Map<String, Object> buildErrorBody(int status, String error, String message, String path) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status);
        body.put("error", error);
        body.put("message", message);
        body.put("path", path);
        return body;
    }
}
