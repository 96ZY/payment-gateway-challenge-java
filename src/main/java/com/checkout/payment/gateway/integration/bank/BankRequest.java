package com.checkout.payment.gateway.integration.bank;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Request payload for the bank simulator API.
 * <p>
 * Contains all payment details required for authorization.
 */
@Data
@AllArgsConstructor
public class BankRequest {

  /** Card number (14-19 digits). */
  @JsonProperty("card_number")
  private String cardNumber;

  /** Expiry date in MM/YYYY format. */
  @JsonProperty("expiry_date")
  private String expiryDate;

  /** CVV/CVC security code (3-4 digits). */
  @JsonProperty("cvv")
  private String cvv;

  /** Amount in minor units (e.g., cents). */
  @JsonProperty("amount")
  private long amount;

  /** Currency code (e.g., "USD", "GBP"). */
  @JsonProperty("currency")
  private String currency;
}