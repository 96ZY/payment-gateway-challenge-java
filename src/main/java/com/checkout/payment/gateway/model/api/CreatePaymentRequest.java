package com.checkout.payment.gateway.model.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request DTO for creating a payment.
 *
 * <p>This class defines the API contract for incoming payment requests.
 * Only minimal validation is applied to ensure the request can be successfully parsed.</p>
 *
 * <p><b>Validation strategy:</b></p>
 * <ul>
 *   <li><b>Technical validation</b> (e.g. missing fields, JSON/type parsing errors)
 *       → HTTP 400 (ErrorResponse)</li>
 *   <li><b>Business validation</b> (e.g. invalid card number, expiry date, currency, CVV, amount)
 *       → PaymentResponse with status = REJECTED</li>
 * </ul>
 *
 * <p>Any request that can be successfully deserialized into this object is considered
 * syntactically valid. All domain-specific validation rules are handled in the service layer
 * and result in a <b>REJECTED</b> payment response if violated.</p>
 *
 * <p><b>Note:</b> Sensitive fields (card number, CVV) must not be logged or persisted in full.</p>
 */
@Data
public class CreatePaymentRequest {

  /** Card number (raw input, validated at service layer). */
  @JsonProperty("card_number")
  @NotBlank
  private String cardNumber;

  /** Expiry month (validated at service layer for range and date logic). */
  @JsonProperty("expiry_month")
  @NotNull
  private Integer expiryMonth;

  /** Expiry year (validated at service layer). */
  @JsonProperty("expiry_year")
  @NotNull
  private Integer expiryYear;

  /** Currency code (validated against supported values at service layer). */
  @JsonProperty("currency")
  @NotBlank
  private String currency;

  /** Amount in minor units (validated for positivity at service layer). */
  @JsonProperty("amount")
  @NotNull
  private Long amount;

  /** CVV/CVC security code (validated at service layer). */
  @JsonProperty("cvv")
  @NotBlank
  private String cvv;
}