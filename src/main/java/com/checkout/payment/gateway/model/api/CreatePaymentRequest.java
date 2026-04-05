package com.checkout.payment.gateway.model.api;

import com.checkout.payment.gateway.validation.ValidCurrency;
import com.checkout.payment.gateway.validation.ValidExpiryDate;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * Request DTO for creating a new payment.
 * <p>
 * Contains validation annotations to ensure data integrity:
 * <ul>
 *   <li>Card number: 14-19 digits</li>
 *   <li>Expiry date: must be in the future</li>
 *   <li>CVV: 3-4 digits</li>
 *   <li>Amount: positive number</li>
 *   <li>Currency: must be a supported currency code</li>
 * </ul>
 */
@ValidExpiryDate
@Data
public class CreatePaymentRequest {

  /** Card number (14-19 digits). */
  @JsonProperty("card_number")
  @NotBlank
  @Pattern(regexp = "\\d{14,19}", message = "Card number must be 14-19 digits")
  private String cardNumber;

  /** Expiry month (1-12). */
  @JsonProperty("expiry_month")
  @NotNull
  @Min(1)
  @Max(12)
  private Integer expiryMonth;

  /** Expiry year (4-digit format, e.g., 2024). */
  @JsonProperty("expiry_year")
  @NotNull
  private Integer expiryYear;

  /** Currency code (e.g., "USD", "GBP", "EUR"). */
  @JsonProperty("currency")
  @NotNull
  @ValidCurrency
  private String currency;

  /** Amount in minor units (e.g., cents for USD). Must be positive. */
  @JsonProperty("amount")
  @NotNull
  @Positive
  private Long amount;

  /** CVV/CVC security code (3-4 digits). */
  @JsonProperty("cvv")
  @NotBlank
  @Pattern(regexp = "\\d{3,4}")
  private String cvv;
}