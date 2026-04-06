package com.checkout.payment.gateway.exception;

import com.checkout.payment.gateway.model.api.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.util.stream.Collectors;

/**
 * Global exception handler for the application.
 * <p>
 * Converts exceptions into standardized HTTP responses with {@link ErrorResponse}. Handles both
 * business logic exceptions and validation errors.
 */
@ControllerAdvice
public class CommonExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(CommonExceptionHandler.class);

  /**
   * Handles business logic exceptions (e.g., payment not found).
   *
   * @param ex the EventProcessingException that was thrown
   * @return ResponseEntity with HTTP 404 Not Found and error message
   */
  @ExceptionHandler(EventProcessingException.class)
  public ResponseEntity<ErrorResponse> handleException(EventProcessingException ex) {
    LOG.error("Exception happened", ex);
    return new ResponseEntity<>(new ErrorResponse(ex.getMessage()),
        HttpStatus.NOT_FOUND);
  }

  /**
   * Handles validation errors from request body validation.
   * <p>
   * Collects all field errors and returns them as a semicolon-separated string.
   *
   * @param ex the MethodArgumentNotValidException containing validation errors
   * @return ResponseEntity with HTTP 400 Bad Request and validation error messages
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
    LOG.error("Exception happened", ex);
    String message = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(e -> e.getField() + " " + e.getDefaultMessage())
        .collect(Collectors.joining("; "));

    return ResponseEntity
        .badRequest()
        .body(new ErrorResponse(message));
  }
}
