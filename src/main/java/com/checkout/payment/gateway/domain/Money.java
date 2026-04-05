package com.checkout.payment.gateway.domain;

import com.checkout.payment.gateway.enums.Currency;
import lombok.Getter;

/**
 * Value object representing a monetary amount with currency.
 * <p>
 * Encapsulates the amount (in minor units, e.g., cents) and currency.
 * This is an immutable value object with validation.
 */
@Getter
public class Money {

  /** Amount in minor currency units (e.g., cents for USD, pence for GBP). */
  private final Long amount;

  /** Currency of the amount. */
  private final Currency currency;

  /**
   * Constructs a new Money value object.
   *
   * @param amount the amount in minor units (must be positive)
   * @param currency the currency (must not be null)
   * @throws IllegalArgumentException if amount is null, zero, or negative; or if currency is null
   */
  public Money(Long amount, Currency currency) {
    if (amount == null || amount <= 0) {
      throw new IllegalArgumentException("Amount must be positive");
    }
    if (currency == null) {
      throw new IllegalArgumentException("Currency must not be null");
    }

    this.amount = amount;
    this.currency = currency;
  }

}