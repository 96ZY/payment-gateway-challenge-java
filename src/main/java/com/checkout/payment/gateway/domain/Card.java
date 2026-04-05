package com.checkout.payment.gateway.domain;

import lombok.Getter;

/**
 * Value object representing a payment card.
 * <p>
 * Stores card information including the last 4 digits (for display purposes),
 * expiry month, and expiry year. This is an immutable value object.
 */
@Getter
public class Card {

  /** Last 4 digits of the card number (for display/masking purposes). */
  private final String last4;

  /** Expiry month (1-12). */
  private final Integer expiryMonth;

  /** Expiry year (4-digit format, e.g., 2024). */
  private final Integer expiryYear;

  /**
   * Constructs a new Card value object.
   * <p>
   * Extracts and stores only the last 4 digits of the card number for security.
   *
   * @param cardNumber the full card number (must be at least 4 digits)
   * @param expiryMonth the expiry month (1-12)
   * @param expiryYear the expiry year (4-digit format)
   * @throws IllegalArgumentException if card number is null or less than 4 digits
   */
  public Card(String cardNumber, Integer expiryMonth, Integer expiryYear) {
    if (cardNumber == null || cardNumber.length() < 4) {
      throw new IllegalArgumentException("Invalid card number");
    }

    this.last4 = cardNumber.substring(cardNumber.length() - 4);
    this.expiryMonth = expiryMonth;
    this.expiryYear = expiryYear;
  }

}