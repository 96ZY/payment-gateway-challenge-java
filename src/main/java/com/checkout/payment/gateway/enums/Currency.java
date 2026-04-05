package com.checkout.payment.gateway.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enumeration of supported currencies for payment processing.
 * <p>
 * Currently supports: USD, EUR, GBP
 */
public enum Currency {

  /**
   * United States Dollar.
   */
  USD,

  /**
   * Euro.
   */
  EUR,

  /**
   * British Pound Sterling.
   */
  GBP;

  /**
   * Deserializes a string value to Currency enum.
   * <p>
   * Case-insensitive matching is supported.
   *
   * @param value the currency string (e.g., "USD", "usd")
   * @return the corresponding Currency enum value
   * @throws IllegalArgumentException if the currency is not supported
   */
  @JsonCreator
  public static Currency from(String value) {
    try {
      return Currency.valueOf(value.toUpperCase());
    } catch (Exception e) {
      throw new IllegalArgumentException("Unsupported currency: " + value);
    }
  }

  /**
   * Serializes the Currency enum to its string representation.
   *
   * @return the currency code (e.g., "USD")
   */
  @JsonValue
  public String toValue() {
    return this.name();
  }
}