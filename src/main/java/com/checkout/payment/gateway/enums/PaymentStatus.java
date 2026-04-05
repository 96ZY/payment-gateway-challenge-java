package com.checkout.payment.gateway.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enumeration representing the possible states of a payment transaction.
 */
public enum PaymentStatus {

  /** Payment was successfully authorized by the bank. */
  AUTHORIZED("Authorized"),

  /** Payment was declined by the bank. */
  DECLINED("Declined"),

  /** Payment was rejected (e.g., due to validation errors). */
  REJECTED("Rejected");

  /** Human-readable display name for the status. */
  private final String name;

  /**
   * Constructs a PaymentStatus with the specified display name.
   *
   * @param name the human-readable name for this status
   */
  PaymentStatus(String name) {
    this.name = name;
  }

  /**
   * Returns the display name for JSON serialization.
   *
   * @return the human-readable status name
   */
  @JsonValue
  public String getName() {
    return this.name;
  }
}
