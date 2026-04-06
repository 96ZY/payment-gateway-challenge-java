package com.checkout.payment.gateway.model.api;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.enums.Currency;
import com.checkout.payment.gateway.util.CardUtil;
import lombok.Data;

import java.util.UUID;

/**
 * Response DTO for payment operations.
 * <p>
 * Contains the payment details returned after processing or retrieval. This is the standard
 * response format for both GET and POST endpoints.
 */
@Data
public class PaymentResponse {

  /**
   * Unique identifier for the payment (may be null if rejected before creation).
   */
  private UUID id;

  /**
   * Current status of the payment (AUTHORIZED, DECLINED, REJECTED).
   */
  private PaymentStatus status;

  /**
   * Last 4 digits of the card number (for display purposes).
   */
  private String cardNumberLastFour;

  /**
   * Expiry month of the card (1-12).
   */
  private int expiryMonth;

  /**
   * Expiry year of the card (4-digit format).
   */
  private int expiryYear;

  /**
   * Currency of the payment.
   */
  private Currency currency;

  /**
   * Amount in minor units (e.g., cents).
   */
  private Long amount;

  /**
   * Authorization code from the bank (null if declined or rejected).
   */
  private String authorizationCode;

  /**
   * Optional message explaining why a payment was rejected.
   */
  private String rejectionReason;

  /**
   * Factory methods for convenience
   */
  public static PaymentResponse rejected(String reason, CreatePaymentRequest request) {
    PaymentResponse response = new PaymentResponse();
    response.setStatus(PaymentStatus.REJECTED);
    response.setRejectionReason(reason);

    if (request != null) {
      response.setCardNumberLastFour(request.getCardNumber() != null ? CardUtil.getLastFourDigits(request.getCardNumber()) : null);
      response.setExpiryMonth(request.getExpiryMonth() != null ? request.getExpiryMonth() : 0);
      response.setExpiryYear(request.getExpiryYear() != null ? request.getExpiryYear() : 0);
      try {
        response.setCurrency(request.getCurrency() != null ? Currency.from(request.getCurrency()) : null);
      } catch (Exception e) {
        // invalid currency
        response.setCurrency(null);
      }
      response.setAmount(request.getAmount());
    }

    return response;
  }
}