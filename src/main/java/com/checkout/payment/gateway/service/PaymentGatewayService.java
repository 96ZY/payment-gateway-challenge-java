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
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PaymentGatewayService {

  private static final Logger LOG = LoggerFactory.getLogger(PaymentGatewayService.class);

  private final PaymentsRepository paymentsRepository;

  private final BankClient bankClient;

  public PaymentGatewayService(PaymentsRepository paymentsRepository, BankClient bankClient) {
    this.paymentsRepository = paymentsRepository;
    this.bankClient = bankClient;
  }

  /**
   * Retrieve a payment by its ID. Converts the domain object into an API response.
   *
   * @param id The ID of the payment to retrieve.
   * @return The payment response.
   */
  public PaymentResponse getPaymentById(UUID id) {
    LOG.debug("Requesting access to to payment with ID {}", id);
    Payment payment = paymentsRepository.get(id)
        .orElseThrow(() -> new EventProcessingException("Payment not found: " + id));
    return PaymentMapper.toResponse(payment);
  }

  /**
   * Process a payment request from the API layer.
   *
   * Flow:
   * 1. Map request DTO to domain model
   * 2. Call external bank service for authorization
   * 3. Update payment status based on bank response
   * 4. Persist the domain object
   * 5. Map domain model to API response
   *
   * @param request The payment request to process.
   * @param idempotencyKey The idempotency key for the request.
   * @return The payment response.
   */
  public PaymentResponse processPayment(CreatePaymentRequest request, String idempotencyKey) {

    if (idempotencyKey == null || idempotencyKey.isBlank()) {
      throw new IllegalArgumentException("Idempotency key is required");
    }

    Payment finalPayment = paymentsRepository.process(idempotencyKey, () -> {

      // Step 1: 构建 payment
      Payment payment = PaymentMapper.toDomain(request);

      try {
        // Step 2: 调 bank
        BankResponse bankResponse = bankClient.authorize(
            request.getCardNumber(),
            request.getCvv(),
            request.getExpiryMonth(),
            request.getExpiryYear(),
            request.getAmount(),
            Currency.from(request.getCurrency())
        );

        // Step 3: 状态
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
}
