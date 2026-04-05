package com.checkout.payment.gateway.integration.bank;

import com.checkout.payment.gateway.enums.Currency;
import com.checkout.payment.gateway.util.CardUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class BankClient {

  private static final Logger LOG = LoggerFactory.getLogger(BankClient.class);

  private final RestTemplate restTemplate;
  private final String bankUrl;

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
  public BankResponse authorize(String cardNumber,
      String cvv,
      int expiryMonth,
      int expiryYear,
      long amount,
      Currency currency) {

    LOG.info("Calling bank simulator...");

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
      LOG.info(
          "Bank request. cardNumberLastFour: {}, expiryDate: {}, amount: {}, currency: {}",
          CardUtil.getLastFourDigits(request.getCardNumber()),
          request.getExpiryDate(),
          request.getAmount(),
          request.getCurrency());

      // Call bank API and deserialize response
      BankResponse response = restTemplate.postForObject(bankUrl, request, BankResponse.class);

      if (response == null) {
        LOG.warn("Bank response is null");
        return new BankResponse(false, null);
      }

      LOG.info("Bank authorized: {}, authorization code: {}", response.isAuthorized(),
          response.getAuthorizationCode());

      return response;
    } catch (Exception e) {
      LOG.error("Error calling bank simulator", e);
      return new BankResponse(false, null);
    }
  }
}