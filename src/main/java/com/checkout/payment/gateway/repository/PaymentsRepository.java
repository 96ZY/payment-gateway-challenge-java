package com.checkout.payment.gateway.repository;

import com.checkout.payment.gateway.domain.Payment;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.springframework.stereotype.Repository;

/**
 * In-memory repository for storing and retrieving payments.
 * <p>
 * Uses {@link ConcurrentHashMap} for thread-safe operations. Supports idempotency by storing
 * payments against idempotency keys.
 */
@Repository
public class PaymentsRepository {

  /**
   * Map for payment storage: payment ID → Payment.
   */
  private final Map<UUID, Payment> payments = new ConcurrentHashMap<>();

  private final Map<String, CompletableFuture<Payment>> idempotencyMap = new ConcurrentHashMap<>();

  /**
   * Retrieves a payment by its unique ID.
   *
   * @param id the payment UUID
   * @return Optional containing the payment if found, empty otherwise
   */
  public Optional<Payment> get(UUID id) {
    return Optional.ofNullable(payments.get(id));
  }

  public Payment process(String key, Supplier<Payment> supplier) {
    CompletableFuture<Payment> future = new CompletableFuture<>();

    CompletableFuture<Payment> existing = idempotencyMap.putIfAbsent(key, future);

    if (existing != null) {
      return existing.join();
    }

    try {
      Payment result = supplier.get();
      future.complete(result);
      payments.put(result.getId(), result);
      return result;
    } catch (Exception e) {
      future.completeExceptionally(e);
      idempotencyMap.remove(key); // 失败允许重试
      throw e;
    }
  }
}