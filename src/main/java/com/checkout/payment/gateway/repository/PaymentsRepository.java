package com.checkout.payment.gateway.repository;

import com.checkout.payment.gateway.domain.Payment;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

/**
 * In-memory repository for storing and retrieving payments.
 * <p>
 * Uses {@link ConcurrentHashMap} for thread-safe operations.
 * Supports idempotency by storing payments against idempotency keys.
 */
@Repository
public class PaymentsRepository {

  /** Map for idempotency: idempotency key → Payment. */
  private final Map<String, Payment> idempotencyMap = new ConcurrentHashMap<>();

  /** Map for payment storage: payment ID → Payment. */
  private final Map<UUID, Payment> payments = new ConcurrentHashMap<>();

  /**
   * Finds a payment by its idempotency key.
   *
   * @param key the idempotency key
   * @return Optional containing the payment if found, empty otherwise
   */
  public Optional<Payment> findByIdempotencyKey(String key) {
    return Optional.ofNullable(idempotencyMap.get(key));
  }

  /**
   * Saves a payment with its idempotency key.
   * <p>
   * Stores the payment in both maps for idempotency and retrieval by ID.
   *
   * @param idempotencyKey the idempotency key for the request
   * @param payment the payment to save
   */
  public void saveWithIdempotency(String idempotencyKey, Payment payment) {
    idempotencyMap.put(idempotencyKey, payment);
    payments.put(payment.getId(), payment);
  }

  /**
   * Retrieves a payment by its unique ID.
   *
   * @param id the payment UUID
   * @return Optional containing the payment if found, empty otherwise
   */
  public Optional<Payment> get(UUID id) {
    return Optional.ofNullable(payments.get(id));
  }
}