package com.checkout.payment.gateway.controller;

import com.checkout.payment.gateway.model.api.CreatePaymentRequest;
import com.checkout.payment.gateway.model.api.PaymentResponse;
import com.checkout.payment.gateway.service.PaymentGatewayService;
import java.util.UUID;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for payment-related operations.
 * <p>
 * Provides endpoints for:
 * <ul>
 *   <li>Retrieving payment details by ID</li>
 *   <li>Processing new payments with idempotency support</li>
 * </ul>
 * <p>
 * Base path: {@code /api}
 */
@Validated
@RestController
@RequestMapping("/api")
public class PaymentGatewayController {

  private final PaymentGatewayService paymentGatewayService;

  /**
   * Constructs a new PaymentGatewayController.
   *
   * @param paymentGatewayService the service layer for payment operations
   */
  public PaymentGatewayController(PaymentGatewayService paymentGatewayService) {
    this.paymentGatewayService = paymentGatewayService;
  }

  /**
   * Retrieves a payment by its unique identifier.
   *
   * @param id the UUID of the payment to retrieve
   * @return ResponseEntity containing the payment details with HTTP 200 OK
   * @throws com.checkout.payment.gateway.exception.EventProcessingException if payment not found
   */
  @GetMapping("/payment/{id}")
  public ResponseEntity<PaymentResponse> getPostPaymentEventById(@PathVariable UUID id) {
    return new ResponseEntity<>(paymentGatewayService.getPaymentById(id), HttpStatus.OK);
  }

  /**
   * Processes a new payment request.
   * <p>
   * This endpoint supports idempotency through the {@code Idempotency-Key} header.
   * Duplicate requests with the same key will return the original response.
   *
   * @param idempotencyKey the idempotency key for the request (required)
   * @param createPaymentRequest the payment details
   * @return ResponseEntity containing the payment result with HTTP 201 CREATED
   */
  @PostMapping("/payment")
  public ResponseEntity<PaymentResponse> postPayment(
      @RequestHeader("Idempotency-Key") String idempotencyKey,
      @RequestBody @Valid CreatePaymentRequest createPaymentRequest) {
    PaymentResponse response = paymentGatewayService.processPayment(createPaymentRequest,
        idempotencyKey);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
}
