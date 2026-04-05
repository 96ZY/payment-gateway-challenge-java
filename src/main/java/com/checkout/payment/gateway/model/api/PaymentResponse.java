package com.checkout.payment.gateway.model.api;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.enums.Currency;
import lombok.Data;

import java.util.UUID;

/**
 * Response DTO for payment operations.
 * <p>
 * Contains the payment details returned after processing or retrieval.
 * This is the standard response format for both GET and POST endpoints.
 */
@Data
public class PaymentResponse {

  /** Unique identifier for the payment. */
  private UUID id;

  /** Current status of the payment (AUTHORIZED, DECLINED, etc.). */
  private PaymentStatus status;

  /** Last 4 digits of the card number (for display purposes). */
  private String cardNumberLastFour;

  /** Expiry month of the card (1-12). */
  private int expiryMonth;

  /** Expiry year of the card (4-digit format). */
  private int expiryYear;

  /** Currency of the payment. */
  private Currency currency;

  /** Amount in minor units (e.g., cents). */
  private Long amount;

  /** Authorization code from the bank (null if declined). */
  private String authorizationCode;
}