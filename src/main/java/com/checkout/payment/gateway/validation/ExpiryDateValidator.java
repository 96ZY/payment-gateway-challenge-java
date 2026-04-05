package com.checkout.payment.gateway.validation;

import com.checkout.payment.gateway.model.api.CreatePaymentRequest;
import com.checkout.payment.gateway.util.CardUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.YearMonth;

/**
 * Validator implementation for {@link ValidExpiryDate} annotation.
 * <p>
 * Validates that the card expiry date (month/year combination) is in the future.
 */
public class ExpiryDateValidator implements
    ConstraintValidator<ValidExpiryDate, CreatePaymentRequest> {

  private static final Logger LOG = LoggerFactory.getLogger(ExpiryDateValidator.class);

  /**
   * Validates the expiry date of a payment request.
   * <p>
   * Returns true if the expiry date is in the future or if either field is null (null handling is
   * delegated to @NotNull annotations).
   *
   * @param request the payment request to validate
   * @param context the validation context for building constraint violations
   * @return true if valid, false otherwise
   */
  @Override
  public boolean isValid(CreatePaymentRequest request, ConstraintValidatorContext context) {
    if (request == null || request.getExpiryMonth() == null || request.getExpiryYear() == null) {
      return true; // let @NotNull handle null cases
    }

    try {
      YearMonth expiry = YearMonth.of(request.getExpiryYear(), request.getExpiryMonth());
      YearMonth now = YearMonth.now();

      if (expiry.isBefore(now)) {
        // Bind class-level error to the expiryMonth field for better UX
        context.disableDefaultConstraintViolation();

        context.buildConstraintViolationWithTemplate(
                context.getDefaultConstraintMessageTemplate())
            .addPropertyNode("expiryMonth")
            .addConstraintViolation();

        return false;
      }
      return true;
    } catch (Exception e) {
      LOG.info("Bank request. cardNumberLastFour: {}, expiryDate: {}, currency: {}, ",
          CardUtil.getLastFourDigits(request.getCardNumber()),
          request.getExpiryMonth() + "/" + request.getExpiryYear(),
          request.getCurrency());
      return false;
    }
  }
}