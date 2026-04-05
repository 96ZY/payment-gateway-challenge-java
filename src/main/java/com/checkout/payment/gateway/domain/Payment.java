package com.checkout.payment.gateway.domain;

import com.checkout.payment.gateway.enums.PaymentStatus;
import lombok.Getter;
import java.util.UUID;

/**
 * Domain entity representing a payment transaction.
 * <p>
 * This is an immutable value object that encapsulates all payment-related information.
 * Modifications create new instances rather than mutating state.
 */
@Getter
public class Payment {

  /** Unique identifier for the payment. */
  private final UUID id;

  /** Card information used for the payment. */
  private final Card card;

  /** Monetary amount and currency of the payment. */
  private final Money money;

  /** Current status of the payment (e.g., AUTHORIZED, DECLINED). */
  private final PaymentStatus status;

  /** Authorization code returned by the bank (null if not authorized). */
  private final String authorizationCode;

  /**
   * Constructs a new Payment.
   *
   * @param id the unique payment identifier
   * @param card the card used for payment
   * @param money the amount and currency
   * @param status the payment status
   * @param authorizationCode the bank authorization code
   */
  public Payment(UUID id, Card card, Money money, PaymentStatus status, String authorizationCode) {
    this.id = id;
    this.card = card;
    this.money = money;
    this.status = status;
    this.authorizationCode = authorizationCode;
  }

  /**
   * Creates a new Payment instance with the specified status.
   * <p>
   * This maintains immutability by returning a new object rather than modifying state.
   *
   * @param status the new payment status
   * @return a new Payment instance with updated status
   */
  public Payment withStatus(PaymentStatus status) {
    return new Payment(this.id, this.card, this.money, status, this.authorizationCode);
  }

  /**
   * Creates a new Payment instance with the specified authorization code.
   * <p>
   * This maintains immutability by returning a new object rather than modifying state.
   *
   * @param code the authorization code from the bank
   * @return a new Payment instance with updated authorization code
   */
  public Payment withAuthorization(String code) {
    return new Payment(id, card, money, status, code);
  }

}