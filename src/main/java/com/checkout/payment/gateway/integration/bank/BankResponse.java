package com.checkout.payment.gateway.integration.bank;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response payload from the bank simulator API.
 * <p>
 * Contains the authorization result and authorization code.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BankResponse {

  /** Whether the payment was authorized by the bank. */
  @JsonProperty("authorized")
  private boolean authorized;

  /** Authorization code returned by the bank (null if not authorized). */
  @JsonProperty("authorization_code")
  private String authorizationCode;

}