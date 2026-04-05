package com.checkout.payment.gateway.mapper;

import com.checkout.payment.gateway.domain.*;
import com.checkout.payment.gateway.enums.Currency;
import com.checkout.payment.gateway.model.api.CreatePaymentRequest;
import com.checkout.payment.gateway.model.api.PaymentResponse;

import java.util.UUID;

/**
 * Mapper class for converting between API DTOs and domain objects.
 * <p>
 * Provides static methods for:
 * <ul>
 *   <li>Converting {@link CreatePaymentRequest} to {@link Payment} domain object</li>
 *   <li>Converting {@link Payment} domain object to {@link PaymentResponse}</li>
 * </ul>
 */
public class PaymentMapper {

  /**
   * Converts a CreatePaymentRequest DTO to a Payment domain object.
   * <p>
   * Generates a new UUID for the payment. Status and authorization code are set to null
   * as they will be determined after bank authorization.
   *
   * @param request the API request containing payment details
   * @return a new Payment domain object
   */
  public static Payment toDomain(CreatePaymentRequest request) {

    Card card = new Card(
        request.getCardNumber(),
        request.getExpiryMonth(),
        request.getExpiryYear()
    );

    Money money = new Money(
        request.getAmount(),
        Currency.from(request.getCurrency())
    );

    return new Payment(
        UUID.randomUUID(),
        card,
        money,
        null,
        null
    );
  }

  /**
   * Converts a Payment domain object to a PaymentResponse DTO.
   *
   * @param payment the domain object to convert
   * @return a populated PaymentResponse for API consumption
   */
  public static PaymentResponse toResponse(Payment payment) {

    PaymentResponse response = new PaymentResponse();

    response.setId(payment.getId());
    response.setAmount(payment.getMoney().getAmount());
    response.setCurrency(payment.getMoney().getCurrency());
    response.setExpiryMonth(payment.getCard().getExpiryMonth());
    response.setExpiryYear(payment.getCard().getExpiryYear());
    response.setCardNumberLastFour(payment.getCard().getLast4());
    response.setStatus(payment.getStatus());
    response.setAuthorizationCode(payment.getAuthorizationCode());

    return response;
  }
}