package com.checkout.payment.gateway.validation;

import com.checkout.payment.gateway.enums.Currency;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;

/**
 * Validator implementation for {@link ValidCurrency} annotation.
 * <p>
 * Validates that a currency string matches one of the supported currencies.
 * Case-insensitive matching is supported.
 */
public class CurrencyValidator implements ConstraintValidator<ValidCurrency, String> {

  /**
   * Validates that the currency code is supported.
   * <p>
   * Performs case-insensitive matching against {@link Currency} enum values.
   *
   * @param value the currency code to validate
   * @param context the validation context
   * @return true if the currency is valid, false otherwise
   */
  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.isBlank()) {
      return false; // null or blank is not valid
    }
    // Check if the value matches any supported currency (case-insensitive)
    return Arrays.stream(Currency.values())
        .anyMatch(c -> c.name().equalsIgnoreCase(value));
  }
}