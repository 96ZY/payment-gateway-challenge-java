package com.checkout.payment.gateway.integration.bank;

import com.checkout.payment.gateway.enums.Currency;
import com.checkout.payment.gateway.util.CardUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * BankClient is used to call the bank simulator
 */
@Component
public class BankClient {

  private static final Logger LOG = LoggerFactory.getLogger(BankClient.class);

  /**
   * RestTemplate
   */
  private final RestTemplate restTemplate;

  /**
   * Bank URL
   */
  private final String bankUrl;

  /**
   * Failed bank response
   */
  private static final BankResponse FAILED = new BankResponse(false, null);

  /**
   * BankClient constructor
   *
   * @param restTemplate RestTemplate
   * @param bankUrl      Bank URL
   */
  public BankClient(RestTemplate restTemplate, @Value("${bank.url}") String bankUrl) {
    this.restTemplate = restTemplate;
    this.bankUrl = bankUrl;
  }

  /**
   * Call bank simulator to authorize a payment
   *
   * @param cardNumber  Card number
   * @param cvv         CVV code
   * @param expiryMonth Expiration month
   * @param expiryYear  Expiration year
   * @param amount      Payment amount in minor units (e.g., cents)
   * @param currency    Payment currency
   * @return BankResponse contains authorization result and authorization code
   */
  public BankResponse authorize(String cardNumber, String cvv, int expiryMonth, int expiryYear,
      long amount, Currency currency) {
    validate(cardNumber, cvv, expiryMonth, expiryYear, amount, currency);

    // Format expiry date as MM/YYYY
    String expiryDate = String.format("%02d/%d", expiryMonth, expiryYear);

    // Build request object
    BankRequest request = new BankRequest(
        cardNumber,
        expiryDate,
        cvv,
        amount,
        currency.toValue()
    );

    try {
      LOG.info("Bank request. cardNumberLastFour: {}, amount: {}, currency: {}",
          CardUtil.getLastFourDigits(request.getCardNumber()),
          request.getAmount(),
          request.getCurrency());

      // Call bank API and deserialize response
      BankResponse response = restTemplate.postForObject(bankUrl, request, BankResponse.class);

      if (response == null) {
        LOG.warn("Bank response is null");
        return FAILED;
      }

      LOG.info("Bank authorized: {}, authorization code: {}", response.isAuthorized(),
          response.getAuthorizationCode());

      return response;
    } catch (Exception e) { // Catch all exceptions to prevent external failures from propagating upstream
      LOG.error("Error calling bank simulator", e);
      return FAILED;
    }
  }

  /**
   * Validate input parameters
   *
   * @param cardNumber  Card number
   * @param cvv         CVV code
   * @param expiryMonth Expiration month
   * @param expiryYear  Expiration year
   * @param amount      Payment amount in minor units (e.g., cents)
   * @param currency    Payment currency
   */
  private void validate(String cardNumber, String cvv, int expiryMonth, int expiryYear, long amount,
      Currency currency) {
    if (StringUtils.isBlank(cardNumber)) {
      throw new IllegalArgumentException("cardNumber must not be blank");
    }
    if (StringUtils.isBlank(cvv)) {
      throw new IllegalArgumentException("cvv must not be blank");
    }
    if (expiryMonth < 1 || expiryMonth > 12) {
      throw new IllegalArgumentException("expiryMonth must be between 1 and 12");
    }
    if (expiryYear <= 0) {
      throw new IllegalArgumentException("expiryYear must be positive");
    }
    if (amount <= 0L) {
      throw new IllegalArgumentException("amount must be positive");
    }
    if (currency == null) {
      throw new IllegalArgumentException("currency must not be null");
    }
  }
}