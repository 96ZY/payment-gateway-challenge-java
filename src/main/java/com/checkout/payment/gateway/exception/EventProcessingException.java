package com.checkout.payment.gateway.exception;

/**
 * Exception thrown when a payment processing error occurs.
 * <p>
 * Used for business logic errors such as:
 * <ul>
 *   <li>Payment not found</li>
 *   <li>Invalid payment state transitions</li>
 *   <li>Processing failures</li>
 * </ul>
 */
public class EventProcessingException extends RuntimeException {

  /**
   * Constructs a new EventProcessingException with the specified message.
   *
   * @param message the detail message explaining the error
   */
  public EventProcessingException(String message) {
    super(message);
  }
}
