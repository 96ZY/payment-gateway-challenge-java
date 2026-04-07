package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.domain.Payment;
import com.checkout.payment.gateway.enums.Currency;
import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.EventProcessingException;
import com.checkout.payment.gateway.integration.bank.BankClient;
import com.checkout.payment.gateway.integration.bank.BankResponse;
import com.checkout.payment.gateway.mapper.PaymentMapper;
import com.checkout.payment.gateway.model.api.CreatePaymentRequest;
import com.checkout.payment.gateway.model.api.PaymentResponse;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import java.time.YearMonth;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Core service responsible for handling payment operations.
 *
 * <p>This service orchestrates the end-to-end payment flow:
 * <ul>
 *   <li>Applying business validation rules</li>
 *   <li>Ensuring idempotent request processing</li>
 *   <li>Interacting with the acquiring bank</li>
 *   <li>Persisting final payment outcomes</li>
 * </ul>
 *
 * <p><b>Validation model:</b></p>
 * <ul>
 *   <li>API-level validation (e.g. malformed JSON, missing required fields)
 *       is handled by the controller/framework → HTTP 400</li>
 *   <li>All syntactically valid requests are processed here and validated
 *       against business rules → REJECTED response if invalid</li>
 * </ul>
 *
 * <p><b>Persistence model:</b></p>
 * <ul>
 *   <li>Only payments sent to the acquiring bank are persisted</li>
 *   <li>Rejected requests are not stored</li>
 * </ul>
 *
 * <p><b>Idempotency:</b></p>
 * <ul>
 *   <li>Requests with the same idempotency key are processed exactly once</li>
 *   <li>Concurrent requests share the same in-flight result</li>
 * </ul>
 *
 * <p><b>Security note:</b> Sensitive card data is used transiently and must not be logged or persisted.</p>
 */
@Service
public class PaymentGatewayService {

  private static final Logger LOG = LoggerFactory.getLogger(PaymentGatewayService.class);

  /**
   * Repository for payments.
   */
  private final PaymentsRepository paymentsRepository;

  /**
   * Client for the bank integration.
   */
  private final BankClient bankClient;

  public PaymentGatewayService(PaymentsRepository paymentsRepository, BankClient bankClient) {
    this.paymentsRepository = paymentsRepository;
    this.bankClient = bankClient;
  }

  /**
   * Retrieves a previously processed payment by its identifier.
   *
   * <p>Only payments that were successfully sent to the acquiring bank
   * (i.e. AUTHORIZED or DECLINED) are persisted and retrievable.</p>
   *
   * @param id the payment identifier
   * @return the mapped API response
   * @throws EventProcessingException if the payment does not exist
   */
  public PaymentResponse getPaymentById(UUID id) {
    LOG.debug("Requesting access to to payment with ID {}", id);
    Payment payment = paymentsRepository.get(id)
        .orElseThrow(() -> new EventProcessingException("Payment not found: " + id));
    return PaymentMapper.toResponse(payment);
  }

  /**
   * Processes a payment request.
   *
   * <p><b>Processing flow:</b></p>
   * <ol>
   *   <li>Validate idempotency key</li>
   *   <li>Apply business validations (card format, expiry, CVV, amount, currency)</li>
   *   <li>If validation fails → return REJECTED response</li>
   *   <li>If valid → invoke acquiring bank</li>
   *   <li>Persist the resulting payment (AUTHORIZED or DECLINED)</li>
   * </ol>
   *
   * <p><b>Validation behavior:</b></p>
   * <ul>
   *   <li>All requests reaching this method are assumed to be syntactically valid</li>
   *   <li>Any rule violation results in a REJECTED payment response</li>
   *   <li>No exception is thrown for business validation failures</li>
   * </ul>
   *
   * <p><b>Idempotency guarantees:</b></p>
   * <ul>
   *   <li>The same idempotency key will always return the same result</li>
   *   <li>Concurrent requests wait for the initial execution to complete</li>
   * </ul>
   *
   * @param request the incoming payment request (already parsed and validated at API level)
   * @param idempotencyKey unique key to ensure idempotent processing
   * @return payment response (REJECTED, AUTHORIZED, or DECLINED)
   * @throws IllegalArgumentException if idempotency key is missing
   */
  public PaymentResponse processPayment(CreatePaymentRequest request, String idempotencyKey) {
    if (idempotencyKey == null || idempotencyKey.isBlank()) {
      throw new IllegalArgumentException("Idempotency key is required");
    }

    PaymentResponse rejection = validate(request);
    if (rejection != null) {
      return rejection;
    }

    Payment finalPayment = paymentsRepository.process(idempotencyKey, () -> {
      // Step 1: build payment
      Payment payment = PaymentMapper.toDomain(request);

      try {
        // Step 2: call bank
        BankResponse bankResponse = bankClient.authorize(
            request.getCardNumber(),
            request.getCvv(),
            request.getExpiryMonth(),
            request.getExpiryYear(),
            request.getAmount(),
            Currency.from(request.getCurrency())
        );

        // Step 3: status check
        PaymentStatus status = bankResponse.isAuthorized()
            ? PaymentStatus.AUTHORIZED
            : PaymentStatus.DECLINED;

        Payment result = payment
            .withStatus(status)
            .withAuthorization(bankResponse.getAuthorizationCode());

        LOG.info("Payment processed: id={}, status={}", result.getId(), status);

        return result;
      } catch (Exception e) {
        LOG.error("Bank call failed", e);
        throw e;
      }
    });

    return PaymentMapper.toResponse(finalPayment);
  }

  /**
   * Validates the payment request.
   * @param request the payment request to validate
   * @return a rejected payment response if validation fails, null otherwise
   */
  private PaymentResponse validate(CreatePaymentRequest request) {
    // Card number: 14–19 digits
    if (!request.getCardNumber().matches("\\d{14,19}")) {
      return PaymentResponse.rejected("Invalid card number", request);
    }

    // CVV: 3–4 digits
    if (!request.getCvv().matches("\\d{3,4}")) {
      return PaymentResponse.rejected("Invalid CVV", request);
    }

    // Expiry month range
    if (request.getExpiryMonth() < 1 || request.getExpiryMonth() > 12) {
      return PaymentResponse.rejected("Invalid expiry month", request);
    }

    // Expiry date must be in the future
    if (YearMonth.of(request.getExpiryYear(), request.getExpiryMonth())
        .isBefore(YearMonth.now())) {
      return PaymentResponse.rejected("Card expired", request);
    }

    // Amount must be positive
    if (request.getAmount() <= 0) {
      return PaymentResponse.rejected("Invalid amount", request);
    }

    // Currency validation
    try {
      Currency.from(request.getCurrency());
    } catch (Exception e) {
      return PaymentResponse.rejected("Unsupported currency", request);
    }

    return null;
  }
}
