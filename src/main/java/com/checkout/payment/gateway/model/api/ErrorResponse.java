package com.checkout.payment.gateway.model.api;

import lombok.Getter;

/**
 * Standard error response payload for API errors.
 * <p>
 * Used by {@link com.checkout.payment.gateway.exception.CommonExceptionHandler}
 * to return consistent error messages to API clients.
 */
@Getter
public class ErrorResponse {

  /** Error message describing what went wrong.
   * -- GETTER --
   *  Returns the error message.
   *
   */
  private final String message;

  /**
   * Constructs a new ErrorResponse.
   *
   * @param message the error message to return to the client
   */
  public ErrorResponse(String message) {
    this.message = message;
  }

  /**
   * Returns a string representation of this error response.
   *
   * @return string representation including the message
   */
  @Override
  public String toString() {
    return "ErrorResponse{" +
        "message='" + message + '\'' +
        '}';
  }
}
