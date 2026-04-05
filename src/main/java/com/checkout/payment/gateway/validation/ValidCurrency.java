package com.checkout.payment.gateway.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Validation annotation to ensure a currency code is supported.
 * <p>
 * Validates that the currency code matches one of the supported currencies
 * defined in {@link com.checkout.payment.gateway.enums.Currency}.
 *
 * @see CurrencyValidator
 */
@Documented
@Constraint(validatedBy = CurrencyValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCurrency {

  /** The error message to display when validation fails. */
  String message() default "Invalid currency";

  /** Validation groups (not used). */
  Class<?>[] groups() default {};

  /** Payload for validation (not used). */
  Class<? extends Payload>[] payload() default {};
}