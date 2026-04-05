package com.checkout.payment.gateway.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validation annotation to ensure a card's expiry date is in the future.
 * <p>
 * This is a class-level annotation that validates the combination of
 * expiry month and expiry year.
 *
 * @see ExpiryDateValidator
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ExpiryDateValidator.class)
@Documented
public @interface ValidExpiryDate {

  /** The error message to display when validation fails. */
  String message() default "Card expiry date must be in the future";

  /** Validation groups (not used). */
  Class<?>[] groups() default {};

  /** Payload for validation (not used). */
  Class<? extends Payload>[] payload() default {};
}